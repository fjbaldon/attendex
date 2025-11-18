"use client";

import * as React from "react";
import {useAttributes} from "@/hooks/use-attributes";
import {columns} from "./columns";
import {AttributesDataTable} from "./attributes-data-table";

export default function AttributesPage() {
    const {definitions, isLoading} = useAttributes();

    return (
        <AttributesDataTable
            columns={columns}
            data={definitions}
            isLoading={isLoading}
        />
    );
}
