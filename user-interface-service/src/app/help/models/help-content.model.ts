export type HelpBlock =
  | { type: 'p'; text: string }
  | { type: 'ul'; items: string[] };

export interface ContextHelpContent {
  id: string;
  title: string;
  paragraphs: HelpBlock[];
  seeMoreHelpId?: string;
}

export interface HelpPageContent {
  id: string;
  slug: string;
  title: string;
  summary: string;
  paragraphs: HelpBlock[];
  relatedCpIds?: string[];
}

export interface HelpIndexEntry {
  slug: string;
  title: string;
  summary: string;
}

export interface HelpIndex {
  entries: HelpIndexEntry[];
}
