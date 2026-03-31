export interface IGETAccountTypeByIdResponse {
  id: number;
  classificationId: number;
  description: string;
  active: boolean;
  notes: string;
}

export type GETAccountTypeByIdResponse = IGETAccountTypeByIdResponse;
