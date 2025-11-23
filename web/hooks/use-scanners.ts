import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, PaginatedResponse, ScannerResponse, UserCreateRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

type ScannerCreateRequest = Omit<UserCreateRequest, 'roleId'>;

export const useScanners = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["scanners", page, size];

    const {data, isLoading: isLoadingScanners} = useQuery<PaginatedResponse<ScannerResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/organization/scanners", {
                params: {page, size, sort: "email,asc"},
            });
            return response.data;
        },
    });

    const createScannerMutation = useMutation<
        ScannerResponse,
        AxiosError<ApiErrorResponse>,
        ScannerCreateRequest
    >({
        mutationFn: (newScannerData) => api.post("/api/v1/organization/scanners", newScannerData),
        onSuccess: async () => {
            toast.success("Scanner created successfully!");
            await queryClient.invalidateQueries({queryKey: ["scanners"]});
        },
        onError: (error) => {
            toast.error("Failed to create scanner", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteScannerMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/organization/scanners/${id}`),
        onSuccess: async () => {
            toast.success("Scanner removed successfully!");
            await queryClient.invalidateQueries({queryKey: ["scanners"]});
        },
        onError: (error) => {
            toast.error("Failed to remove scanner", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const toggleScannerStatusMutation = useMutation<
        ScannerResponse,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.patch(`/api/v1/organization/scanners/${id}/status`),
        onSuccess: async () => {
            toast.success("Scanner status updated.");
            await queryClient.invalidateQueries({queryKey: ["scanners"]});
        },
        onError: (error) => {
            toast.error("Failed to update status", {
                description: getErrorMessage(error, "Could not update scanner status."),
            });
        },
    });

    return {
        scannersData: data,
        isLoadingScanners,
        createScanner: createScannerMutation.mutate,
        isCreatingScanner: createScannerMutation.isPending,
        deleteScanner: deleteScannerMutation.mutate,
        isDeletingScanner: deleteScannerMutation.isPending,
        toggleScannerStatus: toggleScannerStatusMutation.mutate,
        isTogglingStatus: toggleScannerStatusMutation.isPending,
    };
};
