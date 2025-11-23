"use client";

import {Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip} from "recharts";
import {AnalyticsBreakdownItem} from "@/types";

interface PieChartViewProps {
    data: AnalyticsBreakdownItem[];
    total: number;
}

const COLORS = [
    "hsl(var(--primary))",
    "#16a34a", // Green
    "#f97316", // Orange
    "#3b82f6", // Blue
    "#f59e0b", // Amber
    "#8b5cf6", // Violet
    "#ec4899", // Pink
    "#14b8a6", // Teal
];

function PieChartComponent({data, total}: PieChartViewProps) {
    const formattedData = data.map(item => ({
        name: item.value,
        value: item.count,
    }));

    return (
        <ResponsiveContainer width="100%" height={350}>
            <PieChart>
                <Tooltip
                    cursor={{fill: 'hsl(var(--muted))'}}
                    content={({active, payload}) => {
                        if (active && payload && payload.length) {
                            const percent = total > 0 ? ((payload[0].value as number / total) * 100).toFixed(1) : 0;
                            return (
                                <div className="rounded-lg border bg-background p-2 shadow-sm">
                                    <div className="grid grid-cols-2 gap-2">
                                        <div className="flex flex-col">
                                            <span className="text-[0.70rem] uppercase text-muted-foreground">
                                                {payload[0].name}
                                            </span>
                                            <span className="font-bold text-muted-foreground">
                                                {payload[0].value} ({percent}%)
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            );
                        }
                        return null;
                    }}
                />
                <Legend layout="vertical" verticalAlign="middle" align="right" iconSize={10}/>
                <Pie
                    data={formattedData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    outerRadius={120}
                    fill="#8884d8"
                    dataKey="value"
                >
                    {formattedData.map((_entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]}/>
                    ))}
                </Pie>
            </PieChart>
        </ResponsiveContainer>
    );
}

export const PieChartView = PieChartComponent;
