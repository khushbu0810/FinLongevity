export interface RetirementExpenseInputSuggestion {
  sourceExpenseId: string;
  sourceExpenseName: string;
  sourceExpenseCategory: string;
  sourceFrequency: string;
  suggestedCategory: string;
  currentMonthlyAmount: number;
  markedEssential: boolean;
}

export interface InvestmentValuationPrefillItem {
  investmentId: string;
  productName: string;
  investmentStatus: string;
  currency: string;
  netInvestedPrincipal: number;
  recordedMarketValue: number | null;
  usableMarketValue: number | null;
  unrealizedGainLoss: number | null;
  valuationDate: string | null;
  valuationSource: string | null;
  valuationAgeDays: number | null;
  valuationStatus: string;
  stale: boolean;
  includedInCurrentRetirementCorpus: boolean;
  warnings: string[];
}

export interface InvestmentValuationCurrencySummary {
  currency: string;
  investmentCount: number;
  availableValuationCount: number;
  staleValuationCount: number;
  unavailableValuationCount: number;
  freshMarketValueTotal: number;
  includedInCurrentRetirementCorpus: boolean;
}

export interface DebtRetirementLiabilityAssessment {
  liabilityId: string;
  name: string;
  category: string;
  outstandingAmount: number;
  currency: string;
  annualInterestRate: number | null;
  monthlyEmi: number | null;
  nextPaymentDate: string | null;
  estimatedMonthsToPayoff: number | null;
  estimatedPayoffDate: string | null;
  estimatedBalanceAtRetirement: number | null;
  retirementOverlapMonths: number | null;
  assessmentStatus: string;
  warnings: string[];
}

export interface DebtRetirementAssessmentSummary {
  currency: string;
  totalOutstandingDebt: number;
  totalMonthlyEmi: number;
  estimatedOutstandingAtRetirementForAssessedLiabilities: number;
  totalLiabilityCount: number;
  assessedLiabilityCount: number;
  continuingIntoRetirementCount: number;
  paidOffBeforeRetirementCount: number;
  nonAmortizingCount: number;
  insufficientDataCount: number;
}

export interface DebtRetirementAssessmentResponse {
  currency: string;
  asOfDate: string;
  currentAge: number | null;
  plannedRetirementAge: number | null;
  estimatedRetirementDate: string | null;
  summary: DebtRetirementAssessmentSummary;
  liabilities: DebtRetirementLiabilityAssessment[];
  warnings: string[];
}

export interface RetirementPlanningPrefillResponse {
  currency: string;
  asOfDate: string;
  currentAge: number | null;
  currentRetirementCorpus: number;
  currentMonthlyIncome: number;
  currentMonthlyExpenses: number;
  expenseSuggestions: RetirementExpenseInputSuggestion[];
  missingPlanningInputs: string[];
  warnings: string[];
  investmentAssessments: unknown[];
  investmentPrincipalEvidenceByCurrency: unknown[];
  investmentValuations: InvestmentValuationPrefillItem[];
  investmentValuationSummaryByCurrency: InvestmentValuationCurrencySummary[];
  debtRetirementAssessment: DebtRetirementAssessmentResponse | null;
}
