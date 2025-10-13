import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, ScannerResponse, UserCreateRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

type ScannerCreateRequest = Omit<UserCreateRequest, 'roleId'>;

export const useScanners = () => {
    const queryClient = useQueryClient();
    const queryKey = ["scanners"];

    const {data: scanners, isLoading: isLoadingScanners} = useQuery<ScannerResponse[]>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/scanners");
            return response.data;
        },
    });

    const createScannerMutation = useMutation<
        ScannerResponse,
        AxiosError<ApiErrorResponse>,
        ScannerCreateRequest
    >({
        mutationFn: (newScannerData) => api.post("/api/v1/scanners", newScannerData),
        onSuccess: async () => {
            toast.success("Scanner created successfully!");
            await queryClient.invalidateQueries({queryKey});
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
        mutationFn: (id) => api.delete(`/api/v1/scanners/${id}`),
        onSuccess: async () => {
            toast.success("Scanner removed successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to remove scanner", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    return {
        scanners: scanners || [],
        isLoadingScanners,
        createScanner: createScannerMutation.mutate,
        isCreatingScanner: createScannerMutation.isPending,
        deleteScanner: deleteScannerMutation.mutate,
        isDeletingScanner: deleteScannerMutation.isPending,
    };
};
