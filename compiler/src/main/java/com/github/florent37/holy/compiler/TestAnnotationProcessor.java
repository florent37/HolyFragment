package com.github.florent37.holy.compiler;

import com.github.florent37.annotations.LogEnter;
import com.github.florent37.annotations.RunOnUiThread;
import com.github.florent37.holy.annotations.Holy;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * Created by florentchampigny on 11/11/2015.
 */
@AutoService(Processor.class)
public class TestAnnotationProcessor extends AbstractProcessor {

    Map<Class, HolyFragmentHolder> holders = new HashMap<>();

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

            //ex: @Holy Integer i;

            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            //ex: i
            String elementName = enclosingElement.getSimpleName().toString();

            //ex : java.lang.Integer
            Name className = enclosingElement.getQualifiedName();

            TypeName type = TypeName.get(elementType);

            findOrCreateHolyFragmentHolder()
        }
    }


    public HolyFragmentHolder findOrCreateHolyFragmentHolder(Class fragmentClass) {
        if (holders.containsKey(fragmentClass))
            return holders.get(fragmentClass);
        else {
            HolyFragmentHolder holder = new HolyFragmentHolder(fragmentClass);
            holders.put(fragmentClass, holder);
            return holder;
        }
    }

    protected String getType(Object arg) {
        if (arg instanceof Integer)
            return "Int";
        if (arg instanceof String)
            return "String";
        return "";
    }

    public void construct(HolyFragmentHolder holder) {
        MethodSpec.Builder newInstanceBuilder = MethodSpec.methodBuilder("newInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(holder.fragmentClass);

        for (Object arg : holder.args) {
            newInstanceBuilder.addParameter(arg.getClass, arg.getName());
        }

        newInstanceBuilder.addStatement("$T bundle = new $T();", Bundle.class, Bundle.class)

        for (Object arg : holder.args) {
            newInstanceBuilder.addStatement("bundle.put$S(\"$S\",$S)", getType(arg), name, name)
        }
        newInstanceBuilder.addStatement("$S fragment = new $S();", holder.fragmentClass, holder.fragmentClass)
                .addStatement("fragment.setArguments(bundle);")
                .addStatement("return fragment;")
                .build();


        MethodSpec bless = MethodSpec.methodBuilder("bless")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(holder.fragmentClass, "fragment")

                .addStatement("Bundle args = fragment.getArguments();")
        for (Object arg : holder.args) {
            newInstanceBuilder.addStatement("fragment.$S = args.get$S(\"$S\");", name, getType(arg), name);
        }

        MethodSpec newInstance = newInstanceBuilder.build();

        TypeSpec helloWorld = TypeSpec.classBuilder("Holy" + holder.fragmentClass.getCanonicalName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(newInstance)
                .addMethod(bless)
                .build();

        JavaFile javaFile = JavaFile.builder(holder.fragmentClass.getPackage(), helloWorld)
                .build();

        javaFile.writeTo(System.out);
    }
}
