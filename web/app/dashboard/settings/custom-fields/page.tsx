"use client";

import * as React from "react";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {columns} from "./columns";
import {CustomFieldsDataTable} from "./custom-fields-data-table";

export default function CustomFieldsPage() {
    const {definitions, isLoading} = useCustomFields();

    return (
        <CustomFieldsDataTable
            columns={columns}
            data={definitions}
            isLoading={isLoading}
        />
    );
}
