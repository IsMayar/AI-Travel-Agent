import { FormEvent, useEffect, useState } from "react";

import type {
  SaveTripRequest,
  SavedTrip,
} from "../features/trips/tripsApi";

interface EditTripModalProps {
  trip: SavedTrip | null;
  isSaving: boolean;
  onClose: () => void;
  onSave: (trip: SavedTrip, values: SaveTripRequest) => Promise<void>;
}

function valuesFromTrip(trip: SavedTrip | null): SaveTripRequest {
  return {
    userMessage: trip?.userMessage ?? "",
    origin: trip?.origin ?? "",
    destination: trip?.destination ?? "",
    budget: trip?.budget ?? 0,
    days: trip?.days ?? 0,
  };
}

export function EditTripModal({
  trip,
  isSaving,
  onClose,
  onSave,
}: EditTripModalProps) {
  const [values, setValues] = useState<SaveTripRequest>(() =>
    valuesFromTrip(trip)
  );

  useEffect(() => {
    setValues(valuesFromTrip(trip));
  }, [trip]);

  if (!trip) {
    return null;
  }

  const activeTrip = trip;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onSave(activeTrip, values);
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        aria-labelledby="edit-trip-title"
        aria-modal="true"
        className="edit-modal"
        role="dialog"
      >
        <div className="modal-header">
          <div>
            <p className="eyebrow">Edit saved trip</p>
            <h2 id="edit-trip-title">
              {trip.origin} to {trip.destination}
            </h2>
          </div>
          <button
            className="secondary-button compact-button"
            onClick={onClose}
            type="button"
          >
            Close
          </button>
        </div>

        <form className="edit-form" onSubmit={handleSubmit}>
          <label htmlFor="edit-user-message">Travel request</label>
          <textarea
            id="edit-user-message"
            onChange={(event) =>
              setValues((current) => ({
                ...current,
                userMessage: event.target.value,
              }))
            }
            rows={4}
            value={values.userMessage}
          />

          <div className="form-grid">
            <label>
              Origin
              <input
                onChange={(event) =>
                  setValues((current) => ({
                    ...current,
                    origin: event.target.value,
                  }))
                }
                type="text"
                value={values.origin}
              />
            </label>
            <label>
              Destination
              <input
                onChange={(event) =>
                  setValues((current) => ({
                    ...current,
                    destination: event.target.value,
                  }))
                }
                type="text"
                value={values.destination}
              />
            </label>
            <label>
              Budget
              <input
                min="1"
                onChange={(event) =>
                  setValues((current) => ({
                    ...current,
                    budget: Number(event.target.value),
                  }))
                }
                type="number"
                value={values.budget}
              />
            </label>
            <label>
              Days
              <input
                min="1"
                onChange={(event) =>
                  setValues((current) => ({
                    ...current,
                    days: Number(event.target.value),
                  }))
                }
                type="number"
                value={values.days}
              />
            </label>
          </div>

          <div className="modal-actions">
            <button className="primary-button" disabled={isSaving} type="submit">
              {isSaving ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
