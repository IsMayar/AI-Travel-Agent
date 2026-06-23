interface BudgetCardProps {
  origin: string;
  destination: string;
  budget: number;
  days: number;
}

export function BudgetCard({
  origin,
  destination,
  budget,
  days,
}: BudgetCardProps) {
  return (
    <article className="budget-card">
      <p className="eyebrow">Trip summary</p>
      <h3>
        {days} days from {origin} to {destination}
      </h3>
      <div className="budget-number">${budget.toLocaleString()}</div>
      <p>Target trip budget</p>
    </article>
  );
}
