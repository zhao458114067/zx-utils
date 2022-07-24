package com.zx.utils.config;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.zx.utils.constant.Constants;
import com.zx.utils.util.ListUtil;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhaoXu
 * @date 2022/7/24 12:55
 */
public class ExpandApiPlugin implements OperationBuilderPlugin {
    @Autowired
    private TypeResolver typeResolver;


    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }

    @Override
    public void apply(OperationContext operationContext) {
        List<Parameter> newParameters = new ArrayList<>();
        OperationBuilder operationBuilder = operationContext.operationBuilder();
        try {
            Field parameterField = operationBuilder.getClass().getDeclaredField("parameters");
            parameterField.setAccessible(true);
            List<Parameter> parameters = (List<Parameter>) parameterField.get(operationBuilder);
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                String name = parameter.getName();

                // reqReplaceMap用来替代参数接入
                if ("reqReplaceMap".equals(name)) {
                    continue;
                }

                // reqObj展开解析
                if (Constants.REQ_OBJ.equals(name)) {
                    ResolvedType resolvedType = parameter.getType().get();
                    Class<?> aClass = Class.forName(resolvedType.getTypeName());
                    Field[] declaredFields = aClass.getDeclaredFields();
                    // 所有属性，除了serialVersionUID
                    for (int i1 = 0; i1 < declaredFields.length; i1++) {
                        Field declaredField = declaredFields[i1];
                        String fieldName = declaredField.getName();
                        if (!"serialVersionUID".equals(fieldName)) {
                            String description = fieldName;
                            ApiModelProperty apiModelProperty = declaredField.getAnnotation(ApiModelProperty.class);
                            // 默认字段名作为描述
                            if (apiModelProperty != null) {
                                description = apiModelProperty.value();
                            }
                            Parameter query = new Parameter(fieldName, description, "", false, true, new ModelRef("string"), Optional.of(typeResolver.resolve(declaredField.getType())), null, "query",
                                    "", false, "", "", new ArrayList<>());
                            newParameters.add(query);
                        }
                    }
                    continue;
                }
                newParameters.add(parameter);
            }
            if (!ListUtil.isEmpty(newParameters)) {
                // 反射替换新的参数
                Field buildParameterField = operationBuilder.getClass().getDeclaredField("parameters");
                buildParameterField.setAccessible(true);
                buildParameterField.set(operationBuilder, newParameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
