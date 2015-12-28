package com.github.florent37.holy.compiler;

import com.github.florent37.holy.annotations.Holy;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.ElementKind.FIELD;

/**
 * Created by florentchampigny on 11/11/2015.
 */
@AutoService(Processor.class)
public class TestAnnotationProcessor extends AbstractProcessor {

    Map<TypeName, HolyFragmentHolder> holders = new HashMap<>();
    Filer filer;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        processHolys(env);

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();

        types.add(Holy.class.getCanonicalName());

        return types;
    }

    protected void processHolys(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Holy.class)) {
            Class annotationClass = Holy.class;
            if (element.getKind() != FIELD) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a field.", annotationClass.getSimpleName()));
            }

            TypeMirror elementType = element.asType();
            if (elementType.getKind() == TypeKind.TYPEVAR) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a field.", annotationClass.getSimpleName()));
            }

            processHoly(element, elementType);
        }
    }

    protected void processHoly(Element element, TypeMirror elementType) {
        //ex: @Holy Integer number;
        String variableName = element.getSimpleName().toString(); //number
        TypeName variableType = TypeName.get(elementType); //int

        //ex : com.github.florent37.MyFragment
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //ex : com.github.florent37.MyFragment
        ClassName enclosingClassName = ClassName.get(enclosingElement);

        //ex: MyFragment
        String elementName = enclosingElement.getSimpleName().toString();

        HolyFragmentHolder holder = findOrCreateHolyFragmentHolder(enclosingClassName,elementName);
        holder.args.add(new Variable(variableType, variableName));

        construct(holder);
    }

    public HolyFragmentHolder findOrCreateHolyFragmentHolder(ClassName fragmentClassName,String elementName) {
        if (holders.containsKey(fragmentClassName))
            return holders.get(fragmentClassName);
        else {
            HolyFragmentHolder holder = new HolyFragmentHolder(fragmentClassName,elementName);
            holders.put(fragmentClassName, holder);
            return holder;
        }
    }

    protected String getType(TypeName typeName) {
        if (typeName == TypeName.INT || typeName == ClassName.get(Integer.class))
            return "Int";
        if (typeName == ClassName.get(String.class))
            return "String";
        return "";
    }

    public void construct(HolyFragmentHolder holder) {
        MethodSpec.Builder newInstanceBuilder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(holder.classNameComplete);

        for (Variable arg : holder.args) {
            newInstanceBuilder.addParameter(arg.typeName, arg.elementName);
        }

        ClassName bundleClass = ClassName.get("android.os", "Bundle");

        newInstanceBuilder.addStatement("$T bundle = new $T()", bundleClass, bundleClass);

        for (Variable arg : holder.args) {
            newInstanceBuilder.addStatement("bundle.put$L"+"($S,$L)",getType(arg.typeName), arg.elementName, arg.elementName);
        }

        newInstanceBuilder.addStatement("$T fragment = new $T()", holder.classNameComplete, holder.classNameComplete)
                .addStatement("fragment.setArguments(bundle)")
                .addStatement("return fragment");

        MethodSpec newInstance = newInstanceBuilder.build();

        MethodSpec.Builder blessBuilder = MethodSpec.methodBuilder("bless")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(holder.classNameComplete, "fragment")
                .addStatement("Bundle args = fragment.getArguments()");

        for (Variable arg : holder.args) {
            blessBuilder.addStatement("fragment.$L = args.get$L($S)", arg.elementName, getType(arg.typeName), arg.elementName);
        }

        MethodSpec bless = blessBuilder.build();

        TypeSpec holyFragment = TypeSpec.classBuilder("Holy" + holder.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(newInstance)
                .addMethod(bless)
                .build();

        JavaFile javaFile = JavaFile.builder(holder.classNameComplete.packageName(), holyFragment)
                .build();

        try {
            javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
