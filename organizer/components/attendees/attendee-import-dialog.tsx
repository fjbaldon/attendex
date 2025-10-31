"use client";
import * as React from "react";
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {AttendeeImportAnalysis} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {UploadStep} from "./upload-step";
import {ReviewStep} from "./review-step";
import {SuccessStep} from "./success-step";

interface AttendeeImportDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function AttendeeImportDialog({open, onOpenChange}: AttendeeImportDialogProps) {
    const [step, setStep] = React.useState<"upload" | "review" | "success">("upload");
    const [analysisResult, setAnalysisResult] = React.useState<AttendeeImportAnalysis | null>(null);
    const [importedCount, setImportedCount] = React.useState(0);

    const {analyzeAttendees, isAnalyzingAttendees} = useAttendees();

    React.useEffect(() => {
        if (!open) {
            // Reset state when dialog is closed
            setTimeout(() => {
                setStep("upload");
                setAnalysisResult(null);
                setImportedCount(0);
            }, 300);
        }
    }, [open]);

    const handleFileAnalyze = async (file: File) => {
        try {
            const result = await analyzeAttendees(file);
            setAnalysisResult(result);
            setStep("review");
        } catch (error) {
            // Error toast is already handled by the hook
            console.error("Analysis failed:", error);
        }
    };

    const handleCommitSuccess = (count: number) => {
        setImportedCount(count);
        setStep("success");
    };

    const handleStartOver = () => {
        setAnalysisResult(null);
        setStep("upload");
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-4xl max-h-[90vh] flex flex-col p-0">
                <DialogHeader className="p-6 pb-4">
                    <DialogTitle>Import Attendees from CSV</DialogTitle>
                </DialogHeader>
                <div className="flex-grow overflow-y-auto px-6 pb-6">
                    {step === "upload" && (
                        <UploadStep onFileSelect={handleFileAnalyze} isAnalyzing={isAnalyzingAttendees}/>
                    )}
                    {step === "review" && analysisResult && (
                        <ReviewStep
                            analysisResult={analysisResult}
                            onCommitSuccess={handleCommitSuccess}
                            onStartOver={handleStartOver}
                        />
                    )}
                    {step === "success" && (
                        <SuccessStep count={importedCount} onClose={() => onOpenChange(false)}/>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
}
