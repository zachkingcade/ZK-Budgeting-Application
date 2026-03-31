export interface IPOSTUpdateAccountTypeRequest {
  id: number;
  description?: string;
  active?: boolean;
  notes?: string;
}

export type POSTUpdateAccountTypeRequest = IPOSTUpdateAccountTypeRequest;
