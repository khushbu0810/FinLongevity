export type InsuranceDecision = 'APPROVE' | 'REJECT';
export type PremiumFrequency = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'SINGLE';

export interface InsuranceDecisionRequest {
  decision: InsuranceDecision;
  decisionNote?: string;
  premiumAmount?: number;
  premiumFrequency?: PremiumFrequency;
  startDate?: string;
  endDate?: string;
  nextPremiumDueDate?: string;
}
