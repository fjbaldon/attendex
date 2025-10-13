import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {
    ApiErrorResponse,
    AttendeeImportResponse,
    AttendeeRequest,
    AttendeeResponse,
    PaginatedResponse
} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useAttendees = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["attendees", page, size];

    const {data, isLoading: isLoadingAttendees, error: attendeesError} = useQuery<PaginatedResponse<AttendeeResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/attendees", {
                params: {page, size, sort: "lastName,asc"},
            });
            return response.data;
        },
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

    const importAttendeesMutation = useMutation<
        AttendeeImportResponse,
        AxiosError<ApiErrorResponse>,
        File
    >({
        mutationFn: (file) => {
            const formData = new FormData();
            formData.append("file", file);
            return api.post("/api/v1/attendees/import", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            }).then(res => res.data);
        },
        onSuccess: async (data) => {
            toast.success("CSV Import Finished", {
                description: `${data.successfulImports} successful, ${data.failedImports} failed.`,
            });
            if (data.errors && data.errors.length > 0) {
                const errorsToShow = data.errors.slice(0, 3).join("\n");
                toast.warning("Some rows had issues:", {
                    description: errorsToShow + (data.errors.length > 3 ? "\n..." : ""),
                });
            }
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            const errorMessage = getErrorMessage(error, "An unknown error occurred during import.");
            toast.error("Failed to import CSV", {
                description: errorMessage,
            });
        },
    });

    return {
        attendees: data?.content || [],
        pageInfo: data,
        isLoadingAttendees,
        attendeesError,

        createAttendee: createAttendeeMutation.mutate,
        isCreatingAttendee: createAttendeeMutation.isPending,

        updateAttendee: updateAttendeeMutation.mutate,
        isUpdatingAttendee: updateAttendeeMutation.isPending,

        deleteAttendee: deleteAttendeeMutation.mutate,
        isDeletingAttendee: deleteAttendeeMutation.isPending,

        importAttendees: importAttendeesMutation.mutate,
        isImportingAttendees: importAttendeesMutation.isPending,
    };
};
