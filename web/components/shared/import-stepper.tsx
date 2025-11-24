import {cn} from "@/lib/utils";
import {IconCheck} from "@tabler/icons-react";

interface ImportStepperProps {
    currentStep: string;
    steps: { id: string; label: string }[];
}

export function ImportStepper({ currentStep, steps }: ImportStepperProps) {
    const currentIndex = steps.findIndex(s => s.id === currentStep);

    return (
        <div className="relative flex items-center justify-center w-full mb-8">
            {steps.map((step, index) => {
                const isCompleted = index < currentIndex;
                const isCurrent = index === currentIndex;

                return (
                    <div key={step.id} className="flex items-center relative z-10">
                        <div className={cn(
                            "flex flex-col items-center gap-2 transition-colors duration-300",
                            isCurrent ? "text-primary" : isCompleted ? "text-primary" : "text-muted-foreground"
                        )}>
                            <div className={cn(
                                "flex items-center justify-center w-8 h-8 rounded-full border-2 transition-all duration-300 bg-background",
                                isCompleted ? "border-primary bg-primary text-primary-foreground" :
                                    isCurrent ? "border-primary ring-4 ring-primary/20" : "border-muted-foreground/30"
                            )}>
                                {isCompleted ? <IconCheck className="w-4 h-4" /> :
                                    <span className="text-xs font-bold">{index + 1}</span>}
                            </div>
                            <span className="text-xs font-medium absolute top-10 w-20 text-center">{step.label}</span>
                        </div>
                        {index < steps.length - 1 && (
                            <div className={cn(
                                "w-12 sm:w-24 h-[2px] mx-2 transition-colors duration-300",
                                index < currentIndex ? "bg-primary" : "bg-muted"
                            )} />
                        )}
                    </div>
                );
            })}
        </div>
    );
}
