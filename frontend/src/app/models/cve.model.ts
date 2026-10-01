export interface KevDetail {
  vendorProject: string;
  product: string;
  vulnerabilityName: string;
  dateAdded: string;
  requiredAction: string;
  knownRansomwareCampaignUse?: string;
}

export interface EnrichedCve {
  id: string;
  description: string;
  publishedDate: string;
  lastModifiedDate: string;
  cvssScore: number | null;
  severity: string;
  inKev: boolean;
  kevDetail?: KevDetail;
  epssScore: number | null;
  epssPercentile: number | null;
  matchedKeywords: string[];
  isAlert: boolean;
  nvdUrl: string;
}

export interface CveStats {
  totalTracked: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  kevCount: number;
  alertCount: number;
  avgEpss: number;
  severityDistribution: Record<string, number>;
  topTechnologies: Record<string, number>;
  lastSyncTimestamp: string;
}

export interface IndexData {
  lastSync: string;
  stats: CveStats;
  cves: EnrichedCve[];
}
