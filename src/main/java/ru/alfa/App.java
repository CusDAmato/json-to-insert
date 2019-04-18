package ru.alfa;

import com.google.gson.*;

import java.io.*;
import java.util.*;

public class App {

    private static Map<String, StringBuilder> inserts = new HashMap<>();
    private static List<String> parentUids = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            String line = null;
            String lineSeparator = System.getProperty("line.separator");
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(lineSeparator);
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            String jsonContent = stringBuilder.toString();
            parse(jsonContent, null, args[2]);

            File output = new File(args[1]);
            if (!output.exists()) {
                output.createNewFile();
            }

            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output))) {
                if (inserts.get("widgets") != null) {
                    bufferedWriter.write("\n-- widgets\n");
                    bufferedWriter.write(inserts.get("widgets").toString());
                }

                if (inserts.get("widgets_city_groups") != null) {
                    bufferedWriter.write("\n-- widgets_city_groups\n");
                    bufferedWriter.write(inserts.get("widgets_city_groups").toString());
                }

                if (inserts.get("widget_links") != null) {
                    bufferedWriter.write("\n-- widget_links\n");
                    bufferedWriter.write(inserts.get("widget_links").toString());
                }

                if (inserts.get("widget_properties") != null) {
                    bufferedWriter.write("\n-- widget_properties\n");
                    bufferedWriter.write(inserts.get("widget_properties").toString());
                }

                if (inserts.get("widget_property_values") != null) {
                    bufferedWriter.write("\n-- widget_property_values\n");
                    bufferedWriter.write(inserts.get("widget_property_values").toString());
                }

                if (inserts.get("widget_property_values_city_groups") != null) {
                    bufferedWriter.write("\n-- widget_property_values_city_groups\n");
                    bufferedWriter.write(inserts.get("widget_property_values_city_groups").toString());
                }

                bufferedWriter.write("\n-- parents uuid\n");
                for (String parentUid : parentUids) {
                    bufferedWriter.write("-- " + parentUid + "\n");
                }
            }
        }

    }


    private static void parse(String jsonContent, String parentWidget, String deviceType) {
        StringBuilder stringBuilder = null;
        StringBuilder currentValue = null;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = null;
        //widgets
        if ((jsonElement = jsonParser.parse(jsonContent)).isJsonArray()) {
            JsonArray widgeds = jsonElement.getAsJsonArray();
            for (int i = 0; i < widgeds.size(); i++) {
                //widget
                boolean isWidgetHasChildren = false;
                if ((jsonElement = widgeds.get(i)).isJsonObject()) {
                    JsonObject widget = jsonElement.getAsJsonObject();
//                    String widgetUid = widget.get("uid").getAsString();
                    String widgetUid = UUID.randomUUID().toString().replaceAll("-", "");
                    if (parentWidget == null) {
                        parentUids.add(widgetUid);
                    }
                    String widgetName = widget.get("name").getAsString();
                    //properties
                    if ((jsonElement = widget.get("properties")).isJsonArray()) {
                        JsonArray properties = jsonElement.getAsJsonArray();
                        for (int j = 0; j < properties.size(); j++) {
                            //property
                            if ((jsonElement = properties.get(j)).isJsonObject()) {
                                JsonObject property = jsonElement.getAsJsonObject();
                                String propertyName = property.get("name").getAsString();
                                jsonElement = property.get("value");
                                if ("children".equals(propertyName)) {
                                    if (jsonElement.isJsonArray()) {
                                        isWidgetHasChildren = true;
                                        parse(jsonElement.getAsJsonArray().toString(), widgetUid, deviceType);
                                    }
                                } else {
                                    boolean isString = false;
                                    String propertyValue = "null";
                                    if (jsonElement.isJsonArray()) {
                                        propertyValue = jsonElement.getAsJsonArray().toString();
                                    } else if (jsonElement.isJsonObject()) {
                                        propertyValue = jsonElement.getAsJsonObject().toString();
                                    } else if (!jsonElement.isJsonNull()) {
                                        JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                                        if (jsonPrimitive.isString()) {
                                            isString = true;
                                        }
                                        propertyValue = jsonPrimitive.getAsString();
                                    }
                                    String widgetPropertyUid = UUID.randomUUID().toString().replaceAll("-", "");

                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("INSERT INTO content_store.widget_properties ");
                                    stringBuilder.append("(uid, widget_uid, name, device) ");
                                    stringBuilder.append("VALUES ");
                                    stringBuilder.append("(");
                                    stringBuilder.append("'");
                                    stringBuilder.append(widgetPropertyUid);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'");
                                    stringBuilder.append(widgetUid);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'");
                                    stringBuilder.append(propertyName);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'" + deviceType + "'");
                                    stringBuilder.append(");\n");
                                    currentValue = inserts.get("widget_properties");
                                    if (currentValue == null) {
                                        currentValue = new StringBuilder();
                                    }
                                    currentValue.append(stringBuilder);
                                    currentValue.append("\n");
                                    inserts.put("widget_properties", currentValue);


                                    String widgetPropertyValueUid = UUID.randomUUID().toString().replaceAll("-", "");

                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("INSERT INTO content_store.widget_property_values ");
                                    stringBuilder.append("(uid, widget_properties_uid, value) ");
                                    stringBuilder.append("VALUES ");
                                    stringBuilder.append("(");
                                    stringBuilder.append("'");
                                    stringBuilder.append(widgetPropertyValueUid);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'");
                                    stringBuilder.append(widgetPropertyUid);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'");
                                    if (isString) {
                                        stringBuilder.append("\"");
                                    }
                                    stringBuilder.append(propertyValue);
                                    if (isString) {
                                        stringBuilder.append("\"");
                                    }
                                    stringBuilder.append("'");
                                    stringBuilder.append(");\n");
                                    currentValue = inserts.get("widget_property_values");
                                    if (currentValue == null) {
                                        currentValue = new StringBuilder();
                                    }
                                    currentValue.append(stringBuilder);
                                    currentValue.append("\n");
                                    inserts.put("widget_property_values", currentValue);

                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("INSERT INTO content_store.widget_property_values_city_groups ");
                                    stringBuilder.append("(widget_property_values_uid, city_group_uid) ");
                                    stringBuilder.append("VALUES ");
                                    stringBuilder.append("(");
                                    stringBuilder.append("'");
                                    stringBuilder.append(widgetPropertyValueUid);
                                    stringBuilder.append("', ");
                                    stringBuilder.append("'ru'");
                                    stringBuilder.append(");\n");
                                    currentValue = inserts.get("widget_property_values_city_groups");
                                    if (currentValue == null) {
                                        currentValue = new StringBuilder();
                                    }
                                    currentValue.append(stringBuilder);
                                    currentValue.append("\n");
                                    inserts.put("widget_property_values_city_groups", currentValue);

                                }
                            }
                        }
                    }

                    stringBuilder = new StringBuilder();
                    stringBuilder.append("INSERT INTO content_store.widgets ");
                    stringBuilder.append("(uid, name, date_from, date_to, localization, enable, device, has_children) ");
                    stringBuilder.append("VALUES ");
                    stringBuilder.append("(");
                    stringBuilder.append("'");
                    stringBuilder.append(widgetUid);
                    stringBuilder.append("', ");
                    stringBuilder.append("'");
                    stringBuilder.append(widgetName);
                    stringBuilder.append("', ");
                    stringBuilder.append("'2018-01-15 22:39:40.570000', ");
                    stringBuilder.append("'2020-01-15 22:39:47.013000', ");
                    stringBuilder.append("'RU', ");
                    stringBuilder.append("true, ");
                    stringBuilder.append("'" + deviceType + "', ");
                    stringBuilder.append(isWidgetHasChildren);
                    stringBuilder.append(");\n");
                    currentValue = inserts.get("widgets");
                    if (currentValue == null) {
                        currentValue = new StringBuilder();
                    }
                    currentValue.append(stringBuilder);
                    currentValue.append("\n");
                    inserts.put("widgets", currentValue);

                    if (parentWidget != null) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("INSERT INTO content_store.widget_links ");
                        stringBuilder.append("(parent_uid, child_uid, child_sort_id) ");
                        stringBuilder.append("VALUES ");
                        stringBuilder.append("(");
                        stringBuilder.append("'");
                        stringBuilder.append(parentWidget);
                        stringBuilder.append("', ");
                        stringBuilder.append("'");
                        stringBuilder.append(widgetUid);
                        stringBuilder.append("', ");
                        stringBuilder.append("'");
                        stringBuilder.append(i + 1);
                        stringBuilder.append("'");
                        stringBuilder.append(");\n");
                        currentValue = inserts.get("widget_links");
                        if (currentValue == null) {
                            currentValue = new StringBuilder();
                        }
                        currentValue.append(stringBuilder);
                        currentValue.append("\n");
                        inserts.put("widget_links", currentValue);

                    }

                    stringBuilder = new StringBuilder();
                    stringBuilder.append("INSERT INTO content_store.widgets_city_groups ");
                    stringBuilder.append("(widgets_uid, city_group_uid) ");
                    stringBuilder.append("VALUES ");
                    stringBuilder.append("(");
                    stringBuilder.append("'");
                    stringBuilder.append(widgetUid);
                    stringBuilder.append("', ");
                    stringBuilder.append("'ru'");
                    stringBuilder.append(");\n");
                    currentValue = inserts.get("widgets_city_groups");
                    if (currentValue == null) {
                        currentValue = new StringBuilder();
                    }
                    currentValue.append(stringBuilder);
                    currentValue.append("\n");
                    inserts.put("widgets_city_groups", currentValue);

                }
            }
        }
    }
}
