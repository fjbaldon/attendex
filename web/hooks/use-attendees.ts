import {keepPreviousData, useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {
    ApiErrorResponse,
    AttendeeImportAnalysis,
    AttendeeImportCommitRequest,
    AttendeeRequest,
    AttendeeResponse,
    ImportConfiguration,
    PaginatedResponse
} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useAttendees = (page = 0, size = 10, query: string = "") => {
    const queryClient = useQueryClient();
    const queryKey = ["attendees", page, size, query];

    const {
        data,
        isLoading: isLoadingAttendees,
        error: attendeesError,
        refetch
    } = useQuery<PaginatedResponse<AttendeeResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/attendees", {
                params: {page, size, sort: "lastName,asc", query: query || undefined},
            });
            return response.data;
        },
        placeholderData: keepPreviousData,
    });

    const createAttendeeMutation = useMutation<
        AttendeeResponse,
        AxiosError<ApiErrorResponse>,
        AttendeeRequest
    >({
        mutationFn: (newAttendee) => api.post("/api/v1/attendees", newAttendee),
        onSuccess: async () => {
            toast.success("Attendee created successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to create attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateAttendeeMutation = useMutation<
        AttendeeResponse,
        AxiosError<ApiErrorResponse>,
        { id: number; data: AttendeeRequest }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/attendees/${id}`, data),
        onSuccess: async () => {
            toast.success("Attendee updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to update attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteAttendeeMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/attendees/${id}`),
        onSuccess: async () => {
            toast.success("Attendee deleted successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to delete attendee", {
                description: getErrorMessage(error, "This attendee may be registered for an event."),
            });
        },
    });

    // 1. Import Step A: Extract Headers
    const extractHeadersMutation = useMutation<string[], AxiosError<ApiErrorResponse>, File>({
        mutationFn: async (file) => {
            const formData = new FormData();
            formData.append("file", file);
            const response = await api.post("/api/v1/attendees/import/headers", formData, {
                headers: {"Content-Type": "multipart/form-data"},
            });
            return response.data;
        },
        onError: (error) => toast.error("Failed to read CSV headers", {description: getErrorMessage(error, "Unknown error")}),
    });

    // 2. Import Step B: Analyze with Config
    const analyzeAttendeesMutation = useMutation<
        AttendeeImportAnalysis,
        AxiosError<ApiErrorResponse>,
        { file: File; config: ImportConfiguration }
    >({
        mutationFn: async ({file, config}) => {
            const formData = new FormData();
            formData.append("file", file);
            // Send config object as a Blob with application/json Content-Type
            // This allows Spring's @RequestPart to bind it correctly along with the file
            formData.append("config", new Blob([JSON.stringify(config)], {type: "application/json"}));

            const response = await api.post("/api/v1/attendees/import/analyze", formData, {
                headers: {"Content-Type": "multipart/form-data"},
            });
            return response.data;
        },
        onError: (error) => {
            toast.error("Analysis Failed", {description: getErrorMessage(error, "An error occurred during analysis.")});
        },
    });

    // 3. Import Step C: Commit
    const commitAttendeesMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        AttendeeImportCommitRequest
    >({
        mutationFn: (data) => api.post("/api/v1/attendees/import/commit", data),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
            await queryClient.invalidateQueries({queryKey: ["attributes"]}); // Refresh attributes in case new ones were created
        },
        onError: (error) => {
            toast.error("Import Failed", {description: getErrorMessage(error, "An unexpected error occurred while saving the attendees.")});
        },
    });


    return {
        attendeesData: data,
        isLoadingAttendees,
        attendeesError,
        refetch,

        createAttendee: createAttendeeMutation.mutate,
        isCreatingAttendee: createAttendeeMutation.isPending,

        updateAttendee: updateAttendeeMutation.mutate,
        isUpdatingAttendee: updateAttendeeMutation.isPending,

        deleteAttendee: deleteAttendeeMutation.mutate,
        isDeletingAttendee: deleteAttendeeMutation.isPending,

        extractHeaders: extractHeadersMutation.mutateAsync,
        isExtractingHeaders: extractHeadersMutation.isPending,

        analyzeAttendees: analyzeAttendeesMutation.mutateAsync,
        isAnalyzingAttendees: analyzeAttendeesMutation.isPending,

        commitAttendees: commitAttendeesMutation.mutateAsync,
        isCommittingAttendees: commitAttendeesMutation.isPending,
    };
};
