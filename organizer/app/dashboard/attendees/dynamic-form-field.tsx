"use client";

import {Control, FieldValues, Path} from "react-hook-form";
import {CustomFieldDefinition} from "@/types";
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";

interface DynamicFormFieldProps<TFormValues extends FieldValues> {
    fieldDef: CustomFieldDefinition;
    control: Control<TFormValues>;
    name: string;
}

export function DynamicFormField<TFormValues extends FieldValues>({
                                                                      fieldDef,
                                                                      control,
                                                                      name,
                                                                  }: DynamicFormFieldProps<TFormValues>) {
    return (
        <FormField
            control={control}
            name={name as Path<TFormValues>}
            render={({field}) => (
                <FormItem>
                    <FormLabel>{fieldDef.fieldName}</FormLabel>
                    <FormControl>
                        {(() => {
                            switch (fieldDef.fieldType) {
                                case 'NUMBER':
                                    return <Input type="number" placeholder={`Enter ${fieldDef.fieldName}`} {...field}
                                                  onChange={e => field.onChange(e.target.value === '' ? undefined : e.target.valueAsNumber)}/>;
                                case 'DATE':
                                    return <Input type="date" {...field} />;
                                case 'SELECT':
                                    return (
                                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                                            <SelectTrigger><SelectValue
                                                placeholder={`Select ${fieldDef.fieldName}`}/></SelectTrigger>
                                            <SelectContent>
                                                {fieldDef.options?.map(opt => <SelectItem key={opt}
                                                                                          value={opt}>{opt}</SelectItem>)}
                                            </SelectContent>
                                        </Select>
                                    );
                                case 'TEXT':
                                default:
                                    return <Input type="text" placeholder={`Enter ${fieldDef.fieldName}`} {...field} />;
                            }
                        })()}
                    </FormControl>
                    <FormMessage/>
                </FormItem>
            )}
        />
    );
}
