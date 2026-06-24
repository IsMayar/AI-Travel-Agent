import { type FormEvent, useState } from "react";
import { skipToken } from "@reduxjs/toolkit/query";

import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import {
  useAddTripChecklistItemMutation,
  useAddTripNoteMutation,
  useDeleteTripChecklistItemMutation,
  useDeleteTripNoteMutation,
  useGetTripChecklistQuery,
  useGetTripNotesQuery,
  useGetSavedTripQuery,
  useToggleTripChecklistItemMutation,
  useUpdateTripNoteMutation,
} from "../features/trips/tripsApi";

interface TripDetailsPageProps {
  tripIdParam: string;
}

function parseTripId(value: string) {
  const id = Number(value);

  if (!Number.isInteger(id) || id <= 0) {
    return null;
  }

  return id;
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Unknown";
  }

  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function TripDetailsSkeleton() {
  return (
    <div className="trip-details-card" aria-label="Loading trip details">
      <div className="skeleton-card large" />
    </div>
  );
}

export function TripDetailsPage({ tripIdParam }: TripDetailsPageProps) {
  const tripId = parseTripId(tripIdParam);
  const [noteContent, setNoteContent] = useState("");
  const [saveNoteError, setSaveNoteError] = useState("");
  const [updateNoteError, setUpdateNoteError] = useState("");
  const [deleteNoteError, setDeleteNoteError] = useState("");
  const [editingNoteId, setEditingNoteId] = useState<number | null>(null);
  const [editNoteContent, setEditNoteContent] = useState("");
  const [updatingNoteId, setUpdatingNoteId] = useState<number | null>(null);
  const [deletingNoteId, setDeletingNoteId] = useState<number | null>(null);
  const [checklistTitle, setChecklistTitle] = useState("");
  const [checklistError, setChecklistError] = useState("");
  const [togglingChecklistItemId, setTogglingChecklistItemId] = useState<
    number | null
  >(null);
  const [deletingChecklistItemId, setDeletingChecklistItemId] = useState<
    number | null
  >(null);
  const { data: trip, isError, isLoading } = useGetSavedTripQuery(
    tripId ?? skipToken
  );
  const {
    data: notes = [],
    isError: isNotesError,
    isLoading: isNotesLoading,
  } = useGetTripNotesQuery(tripId ?? skipToken);
  const {
    data: checklistItems = [],
    isError: isChecklistError,
    isLoading: isChecklistLoading,
  } = useGetTripChecklistQuery(tripId ?? skipToken);
  const [addTripNote, { isLoading: isSavingNote }] = useAddTripNoteMutation();
  const [updateTripNote, { isLoading: isUpdatingNote }] =
    useUpdateTripNoteMutation();
  const [deleteTripNote, { isLoading: isDeletingNote }] =
    useDeleteTripNoteMutation();
  const [addTripChecklistItem, { isLoading: isAddingChecklistItem }] =
    useAddTripChecklistItemMutation();
  const [toggleTripChecklistItem, { isLoading: isTogglingChecklistItem }] =
    useToggleTripChecklistItemMutation();
  const [deleteTripChecklistItem, { isLoading: isDeletingChecklistItem }] =
    useDeleteTripChecklistItemMutation();

  async function handleNoteSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!tripId || !noteContent.trim()) {
      return;
    }

    setSaveNoteError("");

    try {
      await addTripNote({
        tripId,
        note: { content: noteContent.trim() },
      }).unwrap();
      setNoteContent("");
    } catch {
      setSaveNoteError("Could not save this note. Please try again.");
    }
  }

  function startEditNote(noteId: number, content: string) {
    setEditingNoteId(noteId);
    setEditNoteContent(content);
    setUpdateNoteError("");
  }

  function cancelEditNote() {
    setEditingNoteId(null);
    setEditNoteContent("");
  }

  async function handleUpdateNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!tripId || !editingNoteId || !editNoteContent.trim()) {
      return;
    }

    setUpdateNoteError("");
    setUpdatingNoteId(editingNoteId);

    try {
      await updateTripNote({
        tripId,
        noteId: editingNoteId,
        note: { content: editNoteContent.trim() },
      }).unwrap();
      cancelEditNote();
    } catch {
      setUpdateNoteError("Could not update this note. Please try again.");
    } finally {
      setUpdatingNoteId(null);
    }
  }

  async function handleDeleteNote(noteId: number) {
    if (!tripId || !window.confirm("Delete this note?")) {
      return;
    }

    setDeleteNoteError("");
    setDeletingNoteId(noteId);

    try {
      await deleteTripNote({ tripId, noteId }).unwrap();
    } catch {
      setDeleteNoteError("Could not delete this note. Please try again.");
    } finally {
      setDeletingNoteId(null);
    }
  }

  async function handleChecklistSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!tripId || !checklistTitle.trim()) {
      return;
    }

    setChecklistError("");

    try {
      await addTripChecklistItem({
        tripId,
        item: { title: checklistTitle.trim() },
      }).unwrap();
      setChecklistTitle("");
    } catch {
      setChecklistError("Could not save this checklist item. Please try again.");
    }
  }

  async function handleToggleChecklistItem(itemId: number) {
    if (!tripId) {
      return;
    }

    setChecklistError("");
    setTogglingChecklistItemId(itemId);

    try {
      await toggleTripChecklistItem({ tripId, itemId }).unwrap();
    } catch {
      setChecklistError("Could not update this checklist item.");
    } finally {
      setTogglingChecklistItemId(null);
    }
  }

  async function handleDeleteChecklistItem(itemId: number) {
    if (!tripId || !window.confirm("Delete this checklist item?")) {
      return;
    }

    setChecklistError("");
    setDeletingChecklistItemId(itemId);

    try {
      await deleteTripChecklistItem({ tripId, itemId }).unwrap();
    } catch {
      setChecklistError("Could not delete this checklist item.");
    } finally {
      setDeletingChecklistItemId(null);
    }
  }

  return (
    <main className="simple-page">
      <Container>
        <a className="back-link" href="/saved-trips">
          Back to Saved Trips
        </a>

        <SectionTitle
          eyebrow="Trip details"
          title="Saved trip overview"
          description="Review the basic saved trip information stored by the MVP backend."
        />

        {!tripId && (
          <EmptyState
            actionHref="/saved-trips"
            actionLabel="View Saved Trips"
            message="This trip link is missing a valid saved trip id."
            title="No trip selected"
          />
        )}

        {tripId && isLoading && <TripDetailsSkeleton />}

        {tripId && isError && (
          <ErrorState
            actionLabel="Back to Saved Trips"
            message="This saved trip could not be found or the backend is unavailable."
            onAction={() => {
              window.location.href = "/saved-trips";
            }}
            title="Could not load trip"
          />
        )}

        {tripId && !isLoading && !isError && !trip && (
          <EmptyState
            actionHref="/saved-trips"
            actionLabel="View Saved Trips"
            message="There is no saved trip data to display."
            title="Trip details are empty"
          />
        )}

        {trip && (
          <>
            <article className="trip-details-card">
              <div className="trip-details-hero">
                <p className="eyebrow">Destination</p>
                <h3>{trip.destination}</h3>
                <p>{trip.userMessage}</p>
              </div>

              <div className="details-grid">
                <div>
                  <span>Origin</span>
                  <strong>{trip.origin}</strong>
                </div>
                <div>
                  <span>Destination</span>
                  <strong>{trip.destination}</strong>
                </div>
                <div>
                  <span>Budget</span>
                  <strong>${trip.budget.toLocaleString()}</strong>
                </div>
                <div>
                  <span>Days</span>
                  <strong>{trip.days}</strong>
                </div>
                <div>
                  <span>Created</span>
                  <strong>{formatDate(trip.createdAt)}</strong>
                </div>
              </div>
            </article>

            <section className="trip-notes-card" aria-labelledby="trip-notes">
              <div className="notes-header">
                <div>
                  <p className="eyebrow">Notes</p>
                  <h3 id="trip-notes">Trip notes</h3>
                </div>
              </div>

              <form className="notes-form" onSubmit={handleNoteSubmit}>
                <label htmlFor="trip-note-content">Add a note</label>
                <textarea
                  id="trip-note-content"
                  onChange={(event) => setNoteContent(event.target.value)}
                  placeholder="Add a reminder, idea, or comment for this trip"
                  rows={3}
                  value={noteContent}
                />
                <div className="form-actions">
                  <button
                    className="primary-button"
                    disabled={isSavingNote || !noteContent.trim()}
                    type="submit"
                  >
                    {isSavingNote ? "Saving..." : "Save Note"}
                  </button>
                </div>
              </form>

              {saveNoteError && (
                <p className="preferences-message error-text">
                  {saveNoteError}
                </p>
              )}

              {updateNoteError && (
                <p className="preferences-message error-text">
                  {updateNoteError}
                </p>
              )}

              {deleteNoteError && (
                <p className="preferences-message error-text">
                  {deleteNoteError}
                </p>
              )}

              {isNotesLoading && (
                <div className="notes-list" aria-label="Loading trip notes">
                  <div className="skeleton-card note-skeleton" />
                </div>
              )}

              {!isNotesLoading && isNotesError && (
                <p className="preferences-message error-text">
                  Could not load saved notes.
                </p>
              )}

              {!isNotesLoading && !isNotesError && (
                <div className="notes-list">
                  {notes.length === 0 ? (
                    <p className="notes-empty">No notes saved yet.</p>
                  ) : (
                    notes.map((note) => (
                      <article className="note-item" key={note.id}>
                        {editingNoteId === note.id ? (
                          <form
                            className="inline-note-edit-form"
                            onSubmit={handleUpdateNote}
                          >
                            <label htmlFor={`trip-note-edit-${note.id}`}>
                              Edit note
                            </label>
                            <textarea
                              id={`trip-note-edit-${note.id}`}
                              onChange={(event) =>
                                setEditNoteContent(event.target.value)
                              }
                              rows={3}
                              value={editNoteContent}
                            />
                            <div className="note-actions">
                              <button
                                className="primary-button compact-button"
                                disabled={
                                  isUpdatingNote || !editNoteContent.trim()
                                }
                                type="submit"
                              >
                                {isUpdatingNote && updatingNoteId === note.id
                                  ? "Saving..."
                                  : "Save"}
                              </button>
                              <button
                                className="secondary-button compact-button"
                                disabled={isUpdatingNote}
                                onClick={cancelEditNote}
                                type="button"
                              >
                                Cancel
                              </button>
                            </div>
                          </form>
                        ) : (
                          <p>{note.content}</p>
                        )}
                        <div className="note-item-footer">
                          <time dateTime={note.createdAt}>
                            {formatDate(note.createdAt)}
                          </time>
                          {editingNoteId !== note.id && (
                            <div className="note-actions">
                              <button
                                className="secondary-button compact-button"
                                disabled={isDeletingNote || isUpdatingNote}
                                onClick={() =>
                                  startEditNote(note.id, note.content)
                                }
                                type="button"
                              >
                                Edit
                              </button>
                              <button
                                className="secondary-button compact-button note-delete-button"
                                disabled={isDeletingNote || isUpdatingNote}
                                onClick={() => void handleDeleteNote(note.id)}
                                type="button"
                              >
                                {isDeletingNote && deletingNoteId === note.id
                                  ? "Deleting..."
                                  : "Delete"}
                              </button>
                            </div>
                          )}
                        </div>
                      </article>
                    ))
                  )}
                </div>
              )}
            </section>

            <section
              className="trip-checklist-card"
              aria-labelledby="trip-checklist"
            >
              <div className="notes-header">
                <div>
                  <p className="eyebrow">Checklist</p>
                  <h3 id="trip-checklist">Trip checklist</h3>
                </div>
              </div>

              <form className="checklist-form" onSubmit={handleChecklistSubmit}>
                <label htmlFor="trip-checklist-title">Add checklist item</label>
                <div className="checklist-input-row">
                  <input
                    id="trip-checklist-title"
                    onChange={(event) => setChecklistTitle(event.target.value)}
                    placeholder="Pack passport"
                    type="text"
                    value={checklistTitle}
                  />
                  <button
                    className="primary-button"
                    disabled={isAddingChecklistItem || !checklistTitle.trim()}
                    type="submit"
                  >
                    {isAddingChecklistItem ? "Adding..." : "Add Item"}
                  </button>
                </div>
              </form>

              {checklistError && (
                <p className="preferences-message error-text">
                  {checklistError}
                </p>
              )}

              {isChecklistLoading && (
                <div
                  className="checklist-list"
                  aria-label="Loading trip checklist"
                >
                  <div className="skeleton-card note-skeleton" />
                </div>
              )}

              {!isChecklistLoading && isChecklistError && (
                <p className="preferences-message error-text">
                  Could not load checklist items.
                </p>
              )}

              {!isChecklistLoading && !isChecklistError && (
                <div className="checklist-list">
                  {checklistItems.length === 0 ? (
                    <p className="notes-empty">No checklist items yet.</p>
                  ) : (
                    checklistItems.map((item) => (
                      <article
                        className={`checklist-item${
                          item.completed ? " completed" : ""
                        }`}
                        key={item.id}
                      >
                        <label>
                          <input
                            checked={item.completed}
                            aria-label={
                              isTogglingChecklistItem &&
                              togglingChecklistItemId === item.id
                                ? "Updating checklist item"
                                : "Toggle checklist item"
                            }
                            disabled={
                              isTogglingChecklistItem ||
                              isDeletingChecklistItem
                            }
                            onChange={() =>
                              void handleToggleChecklistItem(item.id)
                            }
                            type="checkbox"
                          />
                          <span>{item.title}</span>
                        </label>
                        <button
                          className="secondary-button compact-button note-delete-button"
                          disabled={
                            isDeletingChecklistItem ||
                            isTogglingChecklistItem
                          }
                          onClick={() =>
                            void handleDeleteChecklistItem(item.id)
                          }
                          type="button"
                        >
                          {isDeletingChecklistItem &&
                          deletingChecklistItemId === item.id
                            ? "Deleting..."
                            : "Delete"}
                        </button>
                      </article>
                    ))
                  )}
                </div>
              )}
            </section>
          </>
        )}
      </Container>
    </main>
  );
}
