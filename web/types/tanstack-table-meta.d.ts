import {RowData} from '@tanstack/react-table';

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog?: (data: TData) => void;
        openDeleteDialog?: (data: TData) => void;
        openResetPasswordDialog?: (data: TData) => void;
        openStatusDialog?: (data: TData) => void;
        openSubscriptionDialog?: (data: TData) => void;
    }
}