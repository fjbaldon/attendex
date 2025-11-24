"use client";

import React, {useState} from 'react';
import {cn} from "@/lib/utils";
import {IconFileSpreadsheet, IconLoader2} from "@tabler/icons-react";
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
        if (file.type !== 'text/csv' && !file.name.endsWith('.csv')) {
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
        <div className="h-full flex flex-col items-center justify-center space-y-8 pb-12 animate-in fade-in slide-in-from-bottom-4">
            {isAnalyzing ? (
                <div className="flex flex-col items-center gap-4">
                    <div className="relative">
                        <div className="absolute inset-0 bg-primary/20 rounded-full animate-ping" />
                        <div className="bg-background p-4 rounded-full border-2 border-primary relative z-10">
                            <IconLoader2 className="h-8 w-8 animate-spin text-primary"/>
                        </div>
                    </div>
                    <div className="text-center">
                        <p className="text-lg font-semibold">Reading file...</p>
                        <p className="text-sm text-muted-foreground">Extracting headers from CSV</p>
                    </div>
                </div>
            ) : (
                <>
                    <div className="text-center space-y-2">
                        <h3 className="text-lg font-semibold">Upload your roster</h3>
                        <p className="text-sm text-muted-foreground max-w-sm mx-auto">
                            Drag and drop your CSV file here, or click to browse.
                            Standard UTF-8 encoded files are supported.
                        </p>
                    </div>

                    <div
                        onDragEnter={handleDragEnter}
                        onDragLeave={handleDragLeave}
                        onDragOver={handleDragEnter}
                        onDrop={handleDrop}
                        onClick={() => fileInputRef.current?.click()}
                        className={cn(
                            "group cursor-pointer relative flex flex-col items-center justify-center w-full max-w-lg h-64 rounded-xl border-2 border-dashed transition-all duration-200 ease-in-out gap-4",
                            isDragging
                                ? "border-primary bg-primary/5 scale-[1.02] ring-4 ring-primary/10"
                                : "border-muted-foreground/25 hover:border-primary/50 hover:bg-muted/20"
                        )}
                    >
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".csv,text/csv"
                            className="hidden"
                            onChange={(e) => handleFile(e.target.files?.[0])}
                        />

                        <div className={cn(
                            "p-4 rounded-full bg-muted transition-colors group-hover:bg-background group-hover:shadow-sm",
                            isDragging && "bg-background shadow-md"
                        )}>
                            <IconFileSpreadsheet className={cn(
                                "h-10 w-10 text-muted-foreground transition-colors",
                                isDragging ? "text-primary" : "group-hover:text-primary"
                            )}/>
                        </div>

                        <div className="space-y-1 text-center">
                            <p className="font-medium">
                                {isDragging ? "Drop file now" : "Select CSV File"}
                            </p>
                            <p className="text-xs text-muted-foreground">
                                Max size: 5MB
                            </p>
                        </div>
                    </div>

                    <div className="flex gap-8 text-xs text-muted-foreground border rounded-full px-6 py-2 bg-muted/30">
                        <div className="flex items-center gap-2">
                            <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                            Identity
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                            First Name
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                            Last Name
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}
