interface PromptChipProps {
  children: string;
  onClick?: () => void;
}

export function PromptChip({ children, onClick }: PromptChipProps) {
  return (
    <button className="prompt-chip" type="button" onClick={onClick}>
      {children}
    </button>
  );
}
