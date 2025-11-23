"use client";

import React, {useState} from 'react';
import {cn} from "@/lib/utils";
import {IconCsv, IconLoader2} from "@tabler/icons-react";
import {toast} from "sonner";

interface UploadStepProps {
    onFileSelect: (file: File) => void;
    isAnalyzing: boolean;
}

export function UploadStep({onFileSelect, isAnalyzing}: UploadStepProps) {
    const [isDragging, setIsDragging] = useState(false);
    const fileInputRef = React.useRef<HTMLInputElement>(null);

    const handleFile = (file: File | null | undefined) => {
        if (!file) return;
        if (file.type !== 'text/csv') {
            toast.error("Invalid File Type", {description: "Please upload a valid .csv file."});
            return;
        }
        onFileSelect(file);
    };

    const handleDragEnter = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(true);
    };

    const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);
    };

    const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);
        handleFile(e.dataTransfer.files[0]);
    };

    return (
        <div className="flex flex-col items-center justify-center space-y-6">
            {isAnalyzing ? (
                <>
                    <IconLoader2 className="h-12 w-12 animate-spin text-primary"/>
                    <div className="text-center">
                        <p className="font-semibold">Analyzing your file...</p>
                        <p className="text-sm text-muted-foreground">This may take a moment for large files.</p>
                    </div>
                </>
            ) : (
                <>
                    <div
                        onDragEnter={handleDragEnter}
                        onDragLeave={handleDragLeave}
                        onDragOver={handleDragEnter}
                        onDrop={handleDrop}
                        onClick={() => fileInputRef.current?.click()}
                        className={cn(
                            "flex w-full flex-col items-center justify-center space-y-4 rounded-lg border-2 border-dashed p-12 text-center transition-colors",
                            isDragging ? "border-primary bg-primary/10" : "border-border hover:border-primary/50"
                        )}
                    >
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".csv"
                            className="hidden"
                            onChange={(e) => handleFile(e.target.files?.[0])}
                        />
                        <div
                            className="flex h-16 w-16 items-center justify-center rounded-full bg-muted text-muted-foreground">
                            <IconCsv className="h-8 w-8"/>
                        </div>
                        <div>
                            <p className="font-semibold">Drag & drop your CSV file here</p>
                            <p className="text-sm text-muted-foreground">or click to browse your files</p>
                        </div>
                    </div>
                    <div className="text-xs text-muted-foreground">
                        Required columns: <code className="font-semibold">identity</code>, <code
                        className="font-semibold">firstName</code>, <code className="font-semibold">lastName</code>
                    </div>
                </>
            )}
        </div>
    );
}
