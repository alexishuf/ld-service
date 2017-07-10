package br.ufsc.inf.lapesd.ldservice.model.impl;

import br.ufsc.inf.lapesd.ldservice.model.Activation;

import javax.annotation.Nonnull;

public class ActivationHelper {
    public static String replace(@Nonnull String template, @Nonnull Activation<Object> activation,
                                 @Nonnull String rxFormat) {
        String result = template;
        for (String varName : activation.getVarNames()) {
            String rx = String.format(rxFormat, varName);
            result = result.replaceAll(rx, activation.get(varName).toString());
        }
        return result;
    }

    public static String replace(@Nonnull String template, @Nonnull Activation activation) {
        return replace(template, activation, "\\$\\{%s\\}");
    }


}
