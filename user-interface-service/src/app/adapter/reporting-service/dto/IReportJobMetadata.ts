export interface IReportJobMetadata {
  id: number;
  reportType: string;
  status: 'QUEUED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  requestParameters: unknown;
  requestedAt: string;
  startedAt: string | null;
  completedAt: string | null;
  failureReason: string | null;
}
