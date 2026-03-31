export interface IPOSTUpdateAccountRequest {
  id: number;
  description?: string;
  notes?: string;
  active?: boolean;
}

export type POSTUpdateAccountRequest = IPOSTUpdateAccountRequest;
