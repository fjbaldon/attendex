"use client";

import {Control, FieldValues, Path} from "react-hook-form";
import {CustomFieldDefinition} from "@/types";
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {IconHash, IconUser} from "@tabler/icons-react";

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
                <FormItem className="flex flex-col gap-1.5">
                    <FormLabel>{fieldDef.fieldName}</FormLabel>
                    <FormControl>
                        {(() => {
                            switch (fieldDef.fieldType) {
                                case 'NUMBER':
                                    return <Input type="number"
                                                  placeholder={`Enter ${fieldDef.fieldName}`}
                                                  {...field}
                                                  value={field.value ?? ""}
                                                  onChange={e => field.onChange(e.target.value === '' ? undefined : e.target.valueAsNumber)}/>;
                                case 'DATE':
                                    return <Input type="date" {...field} value={field.value ?? ""}/>;
                                case 'SELECT':
                                    return (
                                        <Select onValueChange={field.onChange}
                                                value={field.value ? String(field.value) : ""}>
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
                                    return <Input type="text"
                                                  placeholder={`Enter ${fieldDef.fieldName}`}
                                                  {...field}
                                                  value={field.value ?? ""}/>;
                            }
                        })()}
                    </FormControl>
                    <FormMessage/>
                </FormItem>
            )}
        />
    );
}

interface StandardFormFieldProps<TFormValues extends FieldValues> {
    control: Control<TFormValues>;
    name: Path<TFormValues>;
    label: string;
    placeholder: string;
    iconType: 'user' | 'hash';
}

export function StandardFormField<TFormValues extends FieldValues>({
                                                                       control,
                                                                       name,
                                                                       label,
                                                                       placeholder,
                                                                       iconType
                                                                   }: StandardFormFieldProps<TFormValues>) {
    const Icon = iconType === 'user' ? IconUser : IconHash;
    return (
        <FormField
            control={control}
            name={name}
            render={({field}) => (
                <FormItem className="flex flex-col gap-1.5">
                    <FormLabel>{label}</FormLabel>
                    <div className="relative">
                        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                            <Icon className="h-4 w-4 text-muted-foreground"/>
                        </div>
                        <FormControl>
                            <Input placeholder={placeholder} className="pl-10" {...field} value={field.value ?? ""}/>
                        </FormControl>
                    </div>
                    <FormMessage/>
                </FormItem>
            )}
        />
    );
}
