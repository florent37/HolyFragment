package com.github.florent37.holyfragment.processor;

import com.github.florent37.holy.annotations.Holy;
import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import static javax.lang.model.element.ElementKind.FIELD;

/**
 * Created by florentchampigny on 11/11/2015.
 */
@SupportedAnnotationTypes("com.github.florent37.holy.annotations.Holy")
@AutoService(Processor.class)
public class HolyProcessor extends AbstractProcessor {

    Map<TypeName, HolyFragmentHolder> holders = new HashMap<>();
    Set<Element> knownElements = new HashSet<>();
    Filer filer;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        knownElements.clear();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        processHolys(env);

        writeHoldersOnJavaFile();

        return true;
    }

    protected boolean isAcceptable(Element element) {
        if (element.getKind() != FIELD || element.asType().getKind() == TypeKind.TYPEVAR) {
            throw new IllegalStateException("Holy annotation must be on a field.");
        }

        return true;
    }

    protected void processHolys(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Holy.class)) {
            if (!knownElements.contains(element) && isAcceptable(element)) {

                //ex: @Holy Integer number;
                String variableName = element.getSimpleName().toString(); //number
                TypeName variableType = TypeName.get(element.asType()); //int

                //ex : com.github.florent37.MyFragment
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                //ex: MyFragment
                String elementName = enclosingElement.getSimpleName().toString();

                //ex : com.github.florent37.MyFragment
                ClassName enclosingClassName = ClassName.get(enclosingElement);

                HolyFragmentHolder holder = findOrCreateHolyFragmentHolder(enclosingClassName, elementName);
                holder.addArgument(new Variable(variableType, variableName));
            }
        }
    }

    public HolyFragmentHolder findOrCreateHolyFragmentHolder(ClassName fragmentClassName, String elementName) {
        if (holders.containsKey(fragmentClassName))
            return holders.get(fragmentClassName);
        else {
            HolyFragmentHolder holder = new HolyFragmentHolder(fragmentClassName, elementName);
            holders.put(fragmentClassName, holder);
            return holder;
        }
    }

    protected String getType(TypeName typeName) {
        if (typeName == TypeName.INT || typeName == ClassName.get(Integer.class))
            return "Int";
        if (typeName.equals(ClassName.get(String.class)))
            return "String";
        return "";
    }

    protected void writeHoldersOnJavaFile() {
        for (HolyFragmentHolder holder : holders.values()) {
            construct(holder);
        }
        holders.clear();
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
            newInstanceBuilder.addStatement("bundle.put$L" + "($S,$L)", getType(arg.typeName), arg.elementName, arg.elementName);
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
