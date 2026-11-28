export type InvestmentValuationSource = 'USER_REPORTED' | 'PROVIDER_STATEMENT' | 'PROVIDER_FEED';

export interface InvestmentValuationRequest {
  marketValue: number;
  valuationDate: string;
  valuationSource: InvestmentValuationSource;
  sourceReference?: string;
}

export interface InvestmentValuation {
  id: string;
  investmentId: string;
  productNameSnapshot: string;
  marketValue: number;
  currency: string;
  valuationDate: string;
  valuationSource: InvestmentValuationSource;
  sourceReference?: string | null;
  recordedAt: string;
}

export interface InvestmentValuationOverviewItem {
  investmentId: string;
  productNameSnapshot: string;
  investmentStatus: string;
  currency: string;
  netInvestedPrincipal: number;
  latestValuation: InvestmentValuation | null;
  valuationStatus: 'AVAILABLE' | 'UNAVAILABLE' | string;
}

export interface InvestmentValuationOverview {
  investments: InvestmentValuationOverviewItem[];
}
