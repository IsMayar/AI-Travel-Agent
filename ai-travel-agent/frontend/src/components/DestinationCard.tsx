interface DestinationCardProps {
  name: string;
  description: string;
  imageUrl: string;
}

export function DestinationCard({
  name,
  description,
  imageUrl,
}: DestinationCardProps) {
  return (
    <article className="destination-card">
      <img src={imageUrl} alt={`${name} travel destination`} />
      <div>
        <h3>{name}</h3>
        <p>{description}</p>
      </div>
    </article>
  );
}
