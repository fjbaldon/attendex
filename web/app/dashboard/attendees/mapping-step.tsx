"use client";

import React, {useEffect, useState} from "react";
import {Button} from "@/components/ui/button";
import {Select, SelectContent, SelectItem, SelectSeparator, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";
import {ImportConfiguration, ImportMode} from "@/types";
import {useAttributes} from "@/hooks/use-attributes";
import {IconArrowRight, IconSparkles} from "@tabler/icons-react";
import {ScrollArea} from "@/components/ui/scroll-area";
import {toast} from "sonner";
import {Badge} from "@/components/ui/badge";
import {cn} from "@/lib/utils";

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
        {value: "identity", label: "Identity (Required)", keywords: ["id", "identity", "identifier", "student", "code", "number"]},
        {value: "firstName", label: "First Name (Required)", keywords: ["first", "given", "forename"]},
        {value: "lastName", label: "Last Name (Required)", keywords: ["last", "surname", "family"]},
        ...attributes.map(attr => ({value: attr.name, label: `Attribute: ${attr.name}`, keywords: [attr.name.toLowerCase()]}))
    ];

    // Smart Auto-Mapping Logic
    useEffect(() => {
        const newMapping: Record<string, string> = {};

        csvHeaders.forEach(header => {
            const lowerHeader = header.toLowerCase().replace(/[^a-z0-9]/g, ""); // strip symbols

            const match = systemFields.find(field =>
                field.keywords.some(keyword => lowerHeader.includes(keyword))
            );

            if (match) {
                newMapping[header] = match.value;
            }
        });

        if (Object.keys(mapping).length === 0 && Object.keys(newMapping).length > 0) {
            setMapping(newMapping);
            toast.success(`Auto-mapped ${Object.keys(newMapping).length} columns`);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [csvHeaders, attributes]);

    const handleMapChange = (header: string, sysField: string) => {
        setMapping(prev => ({...prev, [header]: sysField}));
    };

    const handleContinue = () => {
        const mappedValues = Object.values(mapping);
        if (!mappedValues.includes("identity")) return toast.error("Map a column to 'Identity'.");
        if (!mappedValues.includes("firstName")) return toast.error("Map a column to 'First Name'.");
        if (!mappedValues.includes("lastName")) return toast.error("Map a column to 'Last Name'.");

        onAnalyze({
            mode,
            createMissingAttributes: createMissing,
            columnMapping: mapping
        });
    };

    return (
        <div className="flex flex-col h-full max-h-[600px]">
            <div className="bg-muted/30 p-4 rounded-lg border mb-4">
                <div className="flex items-center gap-2 mb-4">
                    <IconSparkles className="w-4 h-4 text-primary" />
                    <h3 className="font-medium text-sm">Import Settings</h3>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                        <Label className="text-xs text-muted-foreground uppercase tracking-wider">Duplicate Strategy</Label>
                        <Select value={mode} onValueChange={(v) => setMode(v as ImportMode)}>
                            <SelectTrigger className="bg-background">
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="SKIP">Skip duplicates (Keep existing)</SelectItem>
                                <SelectItem value="UPDATE">Update duplicates (Overwrite)</SelectItem>
                                <SelectItem value="FAIL">Fail on duplicates (Strict)</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="space-y-2">
                        <Label className="text-xs text-muted-foreground uppercase tracking-wider">New Attributes</Label>
                        <div className="flex items-center justify-between bg-background p-2 rounded-md border">
                            <span className="text-sm font-medium pl-1">Auto-create unknown columns</span>
                            <Switch checked={createMissing} onCheckedChange={setCreateMissing}/>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex items-center justify-between px-1 mb-2">
                <div className="font-semibold text-sm">Column Mapping</div>
                <Badge variant="outline" className="font-normal">
                    {Object.keys(mapping).length} / {csvHeaders.length} Mapped
                </Badge>
            </div>

            <ScrollArea className="flex-1 border rounded-md p-0 bg-muted/10">
                <div className="divide-y">
                    {csvHeaders.map((header) => {
                        const isMapped = !!mapping[header];
                        return (
                            <div key={header} className={cn(
                                "grid grid-cols-12 gap-4 items-center p-3 transition-colors",
                                isMapped ? "bg-background" : "bg-muted/20 opacity-70"
                            )}>
                                <div className="col-span-5">
                                    <div className="flex items-center gap-2">
                                        <div className={cn("w-1.5 h-1.5 rounded-full", isMapped ? "bg-green-500" : "bg-gray-300")} />
                                        <span className="truncate text-sm font-medium" title={header}>{header}</span>
                                    </div>
                                </div>
                                <div className="col-span-1 flex justify-center">
                                    <IconArrowRight className="h-4 w-4 text-muted-foreground/50"/>
                                </div>
                                <div className="col-span-6">
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
                                        <SelectTrigger className={cn("h-8", isMapped && "border-primary/50 ring-1 ring-primary/10")}>
                                            <SelectValue placeholder="Ignore"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="ignore" className="text-muted-foreground">-- Ignore Column --</SelectItem>
                                            {systemFields.map(field => (
                                                <SelectItem
                                                    key={field.value}
                                                    value={field.value}
                                                    disabled={Object.values(mapping).includes(field.value) && mapping[header] !== field.value}
                                                >
                                                    {field.label}
                                                </SelectItem>
                                            ))}
                                            {createMissing && (
                                                <>
                                                    <SelectSeparator className="my-2 h-px bg-border" />
                                                    <SelectItem value={header} className="text-blue-600 font-medium">
                                                        {/* Added non-breaking space explicitly here */}
                                                        &nbsp;+ Create new attribute
                                                    </SelectItem>
                                                </>
                                            )}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </ScrollArea>

            <div className="flex justify-between pt-6 pb-4 mt-auto">
                <Button variant="ghost" onClick={onBack}>Back</Button>
                <Button onClick={handleContinue} disabled={isAnalyzing}>
                    {isAnalyzing ? "Analyzing..." : "Review & Import"}
                </Button>
            </div>
        </div>
    );
}
