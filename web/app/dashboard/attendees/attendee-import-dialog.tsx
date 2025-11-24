"use client";
import * as React from "react";
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {AttendeeImportAnalysis, ImportConfiguration} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {UploadStep} from "./upload-step";
import {MappingStep} from "./mapping-step";
import {ReviewStep} from "./review-step";
import {SuccessStep} from "./success-step";
import {ImportStepper} from "@/components/shared/import-stepper"; // Import the new component

interface AttendeeImportDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

type Step = "upload" | "mapping" | "review" | "success";

const IMPORT_STEPS = [
    { id: "upload", label: "Upload CSV" },
    { id: "mapping", label: "Map Columns" },
    { id: "review", label: "Review" },
];

export function AttendeeImportDialog({open, onOpenChange}: AttendeeImportDialogProps) {
    const [step, setStep] = React.useState<Step>("upload");
    // ... keep existing state ...
    const [file, setFile] = React.useState<File | null>(null);
    const [csvHeaders, setCsvHeaders] = React.useState<string[]>([]);
    const [analysisResult, setAnalysisResult] = React.useState<AttendeeImportAnalysis | null>(null);
    const [importedCount, setImportedCount] = React.useState(0);

    const {extractHeaders, isExtractingHeaders, analyzeAttendees, isAnalyzingAttendees} = useAttendees();

    // ... keep existing useEffect ...
    React.useEffect(() => {
        if (!open) {
            setTimeout(() => {
                setStep("upload");
                setFile(null);
                setCsvHeaders([]);
                setAnalysisResult(null);
                setImportedCount(0);
            }, 300);
        }
    }, [open]);

    // ... keep handlers (handleFileSelect, handleAnalyze, handleCommitSuccess) ...
    const handleFileSelect = async (selectedFile: File) => {
        setFile(selectedFile);
        try {
            const headers = await extractHeaders(selectedFile);
            setCsvHeaders(headers);
            setStep("mapping");
        } catch (e) {
            console.error(e);
        }
    };

    const handleAnalyze = async (config: ImportConfiguration) => {
        if (!file) return;
        try {
            const result = await analyzeAttendees({file, config});
            setAnalysisResult(result);
            setStep("review");
        } catch (e) {
            console.error(e);
        }
    };

    const handleCommitSuccess = (count: number) => {
        setImportedCount(count);
        setStep("success");
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-4xl max-h-[90vh] h-[800px] flex flex-col p-0 gap-0 overflow-hidden">
                <DialogHeader className="p-6 pb-2 shrink-0">
                    <DialogTitle className="text-xl text-center mb-4">Import Attendees</DialogTitle>
                    {step !== 'success' && (
                        <ImportStepper currentStep={step} steps={IMPORT_STEPS} />
                    )}
                </DialogHeader>

                <div className="flex-grow overflow-y-auto px-6 pb-6 min-h-0">
                    {step === "upload" && (
                        <UploadStep onFileSelect={handleFileSelect} isAnalyzing={isExtractingHeaders}/>
                    )}
                    {step === "mapping" && (
                        <MappingStep
                            csvHeaders={csvHeaders}
                            onAnalyze={handleAnalyze}
                            isAnalyzing={isAnalyzingAttendees}
                            onBack={() => setStep("upload")}
                        />
                    )}
                    {step === "review" && analysisResult && (
                        <ReviewStep
                            analysisResult={analysisResult}
                            onCommitSuccess={handleCommitSuccess}
                            onStartOver={() => setStep("upload")}
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
