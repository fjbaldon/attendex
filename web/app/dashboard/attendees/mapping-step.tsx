"use client";

import React, {useState} from "react";
import {Button} from "@/components/ui/button";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";
import {ImportConfiguration, ImportMode} from "@/types";
import {useAttributes} from "@/hooks/use-attributes";
import {IconArrowRight} from "@tabler/icons-react";
import {Separator} from "@/components/ui/separator";
import {ScrollArea} from "@/components/ui/scroll-area";
import {toast} from "sonner";

interface MappingStepProps {
    csvHeaders: string[];
    onAnalyze: (config: ImportConfiguration) => void;
    isAnalyzing: boolean;
    onBack: () => void;
}

export function MappingStep({csvHeaders, onAnalyze, isAnalyzing, onBack}: MappingStepProps) {
    const {definitions: attributes} = useAttributes();

    // Configuration State
    const [mode, setMode] = useState<ImportMode>('SKIP');
    const [createMissing, setCreateMissing] = useState(false);
    const [mapping, setMapping] = useState<Record<string, string>>({});

    const systemFields = [
        {value: "identity", label: "Identity (Required)"},
        {value: "firstName", label: "First Name (Required)"},
        {value: "lastName", label: "Last Name (Required)"},
        ...attributes.map(attr => ({value: attr.name, label: `Attribute: ${attr.name}`}))
    ];

    const handleMapChange = (header: string, sysField: string) => {
        setMapping(prev => ({...prev, [header]: sysField}));
    };

    const handleContinue = () => {
        // Validation: Ensure Core Fields are mapped
        const mappedValues = Object.values(mapping);
        if (!mappedValues.includes("identity")) return toast.error("You must map a column to 'Identity'.");
        if (!mappedValues.includes("firstName")) return toast.error("You must map a column to 'First Name'.");
        if (!mappedValues.includes("lastName")) return toast.error("You must map a column to 'Last Name'.");

        onAnalyze({
            mode,
            createMissingAttributes: createMissing,
            columnMapping: mapping
        });
    };

    return (
        <div className="flex flex-col h-full max-h-[600px]">
            <div className="space-y-4 p-1">
                <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <Label>Duplicate Strategy</Label>
                        <Select value={mode} onValueChange={(v) => setMode(v as ImportMode)}>
                            <SelectTrigger>
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="SKIP">Skip duplicates (Keep existing)</SelectItem>
                                <SelectItem value="UPDATE">Update duplicates (Overwrite)</SelectItem>
                                <SelectItem value="FAIL">Fail on duplicates (Strict)</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="flex flex-col justify-center space-y-2">
                        <Label>New Attributes</Label>
                        <div className="flex items-center space-x-2">
                            <Switch checked={createMissing} onCheckedChange={setCreateMissing}/>
                            <span className="text-sm text-muted-foreground">
                                Auto-create attributes for unmapped columns
                            </span>
                        </div>
                    </div>
                </div>

                <Separator/>

                <div className="grid grid-cols-12 gap-4 px-2 font-medium text-sm text-muted-foreground">
                    <div className="col-span-5">CSV Column</div>
                    <div className="col-span-2 flex justify-center"><IconArrowRight className="h-4 w-4"/></div>
                    <div className="col-span-5">System Field</div>
                </div>
            </div>

            <ScrollArea className="flex-1 border rounded-md p-4 mt-2">
                <div className="space-y-4">
                    {csvHeaders.map((header) => (
                        <div key={header} className="grid grid-cols-12 gap-4 items-center">
                            <div className="col-span-5 truncate text-sm font-medium" title={header}>
                                {header}
                            </div>
                            <div className="col-span-2 flex justify-center">
                                <IconArrowRight className="h-4 w-4 text-muted-foreground"/>
                            </div>
                            <div className="col-span-5">
                                <Select
                                    value={mapping[header] || "ignore"}
                                    onValueChange={(val) => val === "ignore" ?
                                        setMapping(prev => {
                                            const next = {...prev};
                                            delete next[header];
                                            return next;
                                        }) : handleMapChange(header, val)
                                    }
                                >
                                    <SelectTrigger className="h-8">
                                        <SelectValue placeholder="Ignore"/>
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="ignore" className="text-muted-foreground">-- Ignore Column
                                            --</SelectItem>
                                        {systemFields.map(field => (
                                            <SelectItem
                                                key={field.value}
                                                value={field.value}
                                                // Disable if already mapped to another column
                                                disabled={Object.values(mapping).includes(field.value) && mapping[header] !== field.value}
                                            >
                                                {field.label}
                                            </SelectItem>
                                        ))}
                                        {createMissing && (
                                            <SelectItem value={header} className="text-blue-600">
                                                Create new: &quot;{header}&quot;
                                            </SelectItem>
                                        )}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                    ))}
                </div>
            </ScrollArea>

            <div className="flex justify-between pt-4 mt-auto">
                <Button variant="ghost" onClick={onBack}>Back</Button>
                <Button onClick={handleContinue} disabled={isAnalyzing}>
                    {isAnalyzing ? "Analyzing..." : "Review & Import"}
                </Button>
            </div>
        </div>
    );
}
