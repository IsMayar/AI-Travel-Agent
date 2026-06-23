interface ErrorStateProps {
  title: string;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function ErrorState({
  title,
  message,
  actionLabel,
  onAction,
}: ErrorStateProps) {
  return (
    <div className="state-panel error-state" role="alert">
      <strong>{title}</strong>
      <p>{message}</p>
      {actionLabel && onAction && (
        <button className="secondary-button" type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
