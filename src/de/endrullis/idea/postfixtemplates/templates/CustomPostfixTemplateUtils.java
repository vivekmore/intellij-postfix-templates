package de.endrullis.idea.postfixtemplates.templates;

import com.intellij.util.containers.OrderedSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilities for custom postfix templates.
 *
 * @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
 */
public class CustomPostfixTemplateUtils {

	/**
	 * Returns the variable names used in the template.
	 *
	 * @param templateText template text
	 * @return the variable names used in the template
	 */
	@NotNull
	static Set<String> parseVariableNames(@NotNull String templateText) {
		Set<String> variableNames = new OrderedSet<>();

		int varStart = -1;
		int i = 0;

		while (i < templateText.length()) {
			char c = templateText.charAt(i);

			if (c == '\\') {
				i++;
			} else if (c == '$') {
				if (varStart == -1) {
					varStart = i;
				} else {
					String varName = templateText.substring(varStart + 1, i);
					variableNames.add(varName);
					varStart = -1;
				}
			}

			i++;
		}

		return variableNames;
	}

	/**
	 * Returns the variables used in the template.
	 *
	 * @param templateText template text
	 * @return the variables used in the template
	 */
	public static List<MyVariable> parseVariables(@NotNull String templateText) {
		Set<String> varNames = parseVariableNames(templateText);

		final int[] autoNo = {0};

		return varNames.stream().map(variable -> {
			String[] parts = variable.split(":", 3);

			String[] nameParts = parts[0].split("#", 2);

			boolean skipIfDefined = nameParts[0].endsWith("*");
			String varName = nameParts[0].replaceFirst("\\*$", "");

			int no;
			try {
				no = nameParts.length == 2 ? Integer.parseInt(nameParts[1]) : autoNo[0];
			} catch (NumberFormatException e) {
				no = autoNo[0];
			}

			autoNo[0]++;

			if (parts.length == 3) {
				return new MyVariable(varName, parts[1], parts[2], true, skipIfDefined, no, variable);
			} else if (parts.length == 2) {
				return new MyVariable(varName, parts[1], "", true, skipIfDefined, no, variable);
			} else {
				return new MyVariable(varName, "", "", true, skipIfDefined, no, variable);
			}
		}).collect(Collectors.toList());
	}

	/**
	 * Returns the template text without the variable default values.
	 *
	 * @param templateText template text
	 * @param variables    variables that may have default values
	 * @return the template text without the variable default values
	 */
	public static String removeVariableValues(@NotNull String templateText, Collection<MyVariable> variables) {
		final String[] newTemplateText = {templateText};

		variables.forEach(variable -> {
			String varPattern = "$" + variable.getVarCode() + "$";
			String replacement = "$" + variable.getName().replaceFirst("\\*$", "") + "$";
			newTemplateText[0] = newTemplateText[0].replaceAll(Pattern.quote(varPattern), Matcher.quoteReplacement(replacement));
		});

		return newTemplateText[0];
	}

}
