export interface ICatalogField {
  name: string;
  description: string;
  type: string;
  /** When true, the UI blocks submit until a value is present (from catalog API). */
  required?: boolean;
}

export interface ICatalogReport {
  code: string;
  displayName: string;
  description: string;
  fields: ICatalogField[];
}
