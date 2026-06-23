interface EmptyStateProps {
  title: string;
  message: string;
  actionLabel?: string;
  actionHref?: string;
}

export function EmptyState({
  title,
  message,
  actionLabel,
  actionHref,
}: EmptyStateProps) {
  return (
    <div className="state-panel empty-state">
      <strong>{title}</strong>
      <p>{message}</p>
      {actionLabel && actionHref && (
        <a className="secondary-button" href={actionHref}>
          {actionLabel}
        </a>
      )}
    </div>
  );
}
