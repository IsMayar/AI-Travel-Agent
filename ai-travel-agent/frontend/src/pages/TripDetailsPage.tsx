import { type FormEvent, useState } from "react";
import { skipToken } from "@reduxjs/toolkit/query";

import { Container } from "../components/Container";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { SectionTitle } from "../components/SectionTitle";
import {
  useAddTripBudgetItemMutation,
  useAddTripChecklistItemMutation,
  useAddTripDocumentMutation,
  useAddTripNoteMutation,
  useDeleteTripBudgetItemMutation,
  useDeleteTripChecklistItemMutation,
  useDeleteTripDocumentMutation,
  useDeleteTripNoteMutation,
  useGetTripBudgetItemsQuery,
  useGetTripChecklistQuery,
  useGetTripDocumentsQuery,
  useGetTripNotesQuery,
  useGetSavedTripQuery,
  useToggleTripChecklistItemMutation,
  useUpdateTripBudgetItemMutation,
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

function formatCurrency(value: number) {
  return new Intl.NumberFormat("en", {
    currency: "USD",
    maximumFractionDigits: 2,
    style: "currency",
  }).format(value);
}

function parseAmount(value: string) {
  const parsedValue = Number(value);

  if (!Number.isFinite(parsedValue)) {
    return null;
  }

  return parsedValue;
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
  const [documentName, setDocumentName] = useState("");
  const [documentType, setDocumentType] = useState("");
  const [documentUrl, setDocumentUrl] = useState("");
  const [documentError, setDocumentError] = useState("");
  const [deletingDocumentId, setDeletingDocumentId] = useState<number | null>(
    null
  );
  const [budgetTitle, setBudgetTitle] = useState("");
  const [budgetCategory, setBudgetCategory] = useState("");
  const [budgetAmount, setBudgetAmount] = useState("");
  const [editingBudgetItemId, setEditingBudgetItemId] = useState<number | null>(
    null
  );
  const [editBudgetTitle, setEditBudgetTitle] = useState("");
  const [editBudgetCategory, setEditBudgetCategory] = useState("");
  const [editBudgetAmount, setEditBudgetAmount] = useState("");
  const [budgetError, setBudgetError] = useState("");
  const [updatingBudgetItemId, setUpdatingBudgetItemId] = useState<
    number | null
  >(null);
  const [deletingBudgetItemId, setDeletingBudgetItemId] = useState<
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
  const {
    data: documents = [],
    isError: isDocumentsError,
    isLoading: isDocumentsLoading,
  } = useGetTripDocumentsQuery(tripId ?? skipToken);
  const {
    data: budgetItems = [],
    isError: isBudgetItemsError,
    isLoading: isBudgetItemsLoading,
  } = useGetTripBudgetItemsQuery(tripId ?? skipToken);
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
  const [addTripDocument, { isLoading: isAddingDocument }] =
    useAddTripDocumentMutation();
  const [deleteTripDocument, { isLoading: isDeletingDocument }] =
    useDeleteTripDocumentMutation();
  const [addTripBudgetItem, { isLoading: isAddingBudgetItem }] =
    useAddTripBudgetItemMutation();
  const [updateTripBudgetItem, { isLoading: isUpdatingBudgetItem }] =
    useUpdateTripBudgetItemMutation();
  const [deleteTripBudgetItem, { isLoading: isDeletingBudgetItem }] =
    useDeleteTripBudgetItemMutation();

  const budgetItemsTotal = budgetItems.reduce(
    (total, item) => total + Number(item.amount || 0),
    0
  );

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

  async function handleDocumentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!tripId || !documentName.trim() || !documentType.trim() || !documentUrl.trim()) {
      return;
    }

    setDocumentError("");

    try {
      await addTripDocument({
        tripId,
        document: {
          name: documentName.trim(),
          type: documentType.trim(),
          url: documentUrl.trim(),
        },
      }).unwrap();
      setDocumentName("");
      setDocumentType("");
      setDocumentUrl("");
    } catch {
      setDocumentError("Could not save this document. Please try again.");
    }
  }

  async function handleDeleteDocument(documentId: number) {
    if (!tripId || !window.confirm("Delete this document?")) {
      return;
    }

    setDocumentError("");
    setDeletingDocumentId(documentId);

    try {
      await deleteTripDocument({ tripId, documentId }).unwrap();
    } catch {
      setDocumentError("Could not delete this document.");
    } finally {
      setDeletingDocumentId(null);
    }
  }

  async function handleBudgetSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const parsedAmount = parseAmount(budgetAmount);
    if (!tripId || !budgetTitle.trim() || !budgetCategory.trim() || parsedAmount === null) {
      return;
    }

    setBudgetError("");

    try {
      await addTripBudgetItem({
        tripId,
        item: {
          amount: parsedAmount,
          category: budgetCategory.trim(),
          title: budgetTitle.trim(),
        },
      }).unwrap();
      setBudgetTitle("");
      setBudgetCategory("");
      setBudgetAmount("");
    } catch {
      setBudgetError("Could not save this budget item. Please try again.");
    }
  }

  function startEditBudgetItem(
    itemId: number,
    title: string,
    category: string,
    amount: number
  ) {
    setEditingBudgetItemId(itemId);
    setEditBudgetTitle(title);
    setEditBudgetCategory(category);
    setEditBudgetAmount(String(amount));
    setBudgetError("");
  }

  function cancelEditBudgetItem() {
    setEditingBudgetItemId(null);
    setEditBudgetTitle("");
    setEditBudgetCategory("");
    setEditBudgetAmount("");
  }

  async function handleUpdateBudgetItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const parsedAmount = parseAmount(editBudgetAmount);
    if (
      !tripId ||
      !editingBudgetItemId ||
      !editBudgetTitle.trim() ||
      !editBudgetCategory.trim() ||
      parsedAmount === null
    ) {
      return;
    }

    setBudgetError("");
    setUpdatingBudgetItemId(editingBudgetItemId);

    try {
      await updateTripBudgetItem({
        tripId,
        itemId: editingBudgetItemId,
        item: {
          amount: parsedAmount,
          category: editBudgetCategory.trim(),
          title: editBudgetTitle.trim(),
        },
      }).unwrap();
      cancelEditBudgetItem();
    } catch {
      setBudgetError("Could not update this budget item.");
    } finally {
      setUpdatingBudgetItemId(null);
    }
  }

  async function handleDeleteBudgetItem(itemId: number) {
    if (!tripId || !window.confirm("Delete this budget item?")) {
      return;
    }

    setBudgetError("");
    setDeletingBudgetItemId(itemId);

    try {
      await deleteTripBudgetItem({ tripId, itemId }).unwrap();
    } catch {
      setBudgetError("Could not delete this budget item.");
    } finally {
      setDeletingBudgetItemId(null);
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

            <section
              className="trip-documents-card"
              aria-labelledby="trip-documents"
            >
              <div className="notes-header">
                <div>
                  <p className="eyebrow">Documents</p>
                  <h3 id="trip-documents">Trip documents</h3>
                </div>
              </div>

              <form className="document-form" onSubmit={handleDocumentSubmit}>
                <div className="metadata-form-grid">
                  <label htmlFor="trip-document-name">
                    Name
                    <input
                      id="trip-document-name"
                      onChange={(event) => setDocumentName(event.target.value)}
                      placeholder="Passport scan"
                      type="text"
                      value={documentName}
                    />
                  </label>
                  <label htmlFor="trip-document-type">
                    Type
                    <input
                      id="trip-document-type"
                      onChange={(event) => setDocumentType(event.target.value)}
                      placeholder="Passport"
                      type="text"
                      value={documentType}
                    />
                  </label>
                  <label htmlFor="trip-document-url">
                    URL
                    <input
                      id="trip-document-url"
                      onChange={(event) => setDocumentUrl(event.target.value)}
                      placeholder="https://example.com/document"
                      type="url"
                      value={documentUrl}
                    />
                  </label>
                </div>
                <div className="form-actions">
                  <button
                    className="primary-button"
                    disabled={
                      isAddingDocument ||
                      !documentName.trim() ||
                      !documentType.trim() ||
                      !documentUrl.trim()
                    }
                    type="submit"
                  >
                    {isAddingDocument ? "Adding..." : "Add Document"}
                  </button>
                </div>
              </form>

              {documentError && (
                <p className="preferences-message error-text">
                  {documentError}
                </p>
              )}

              {isDocumentsLoading && (
                <div
                  className="documents-list"
                  aria-label="Loading trip documents"
                >
                  <div className="skeleton-card note-skeleton" />
                </div>
              )}

              {!isDocumentsLoading && isDocumentsError && (
                <p className="preferences-message error-text">
                  Could not load documents.
                </p>
              )}

              {!isDocumentsLoading && !isDocumentsError && (
                <div className="documents-list">
                  {documents.length === 0 ? (
                    <p className="notes-empty">No documents saved yet.</p>
                  ) : (
                    documents.map((document) => (
                      <article className="document-item" key={document.id}>
                        <div>
                          <strong>{document.name}</strong>
                          <span>{document.type}</span>
                          <a
                            href={document.url}
                            rel="noreferrer"
                            target="_blank"
                          >
                            {document.url}
                          </a>
                        </div>
                        <button
                          className="secondary-button compact-button note-delete-button"
                          disabled={isDeletingDocument}
                          onClick={() =>
                            void handleDeleteDocument(document.id)
                          }
                          type="button"
                        >
                          {isDeletingDocument &&
                          deletingDocumentId === document.id
                            ? "Deleting..."
                            : "Delete"}
                        </button>
                      </article>
                    ))
                  )}
                </div>
              )}
            </section>

            <section
              className="trip-budget-card"
              aria-labelledby="trip-budget-items"
            >
              <div className="notes-header">
                <div>
                  <p className="eyebrow">Budget</p>
                  <h3 id="trip-budget-items">Budget items</h3>
                </div>
                <div className="budget-total">
                  <span>Total</span>
                  <strong>{formatCurrency(budgetItemsTotal)}</strong>
                </div>
              </div>

              <form className="budget-form" onSubmit={handleBudgetSubmit}>
                <div className="metadata-form-grid">
                  <label htmlFor="trip-budget-title">
                    Title
                    <input
                      id="trip-budget-title"
                      onChange={(event) => setBudgetTitle(event.target.value)}
                      placeholder="Flight"
                      type="text"
                      value={budgetTitle}
                    />
                  </label>
                  <label htmlFor="trip-budget-category">
                    Category
                    <input
                      id="trip-budget-category"
                      onChange={(event) =>
                        setBudgetCategory(event.target.value)
                      }
                      placeholder="Transport"
                      type="text"
                      value={budgetCategory}
                    />
                  </label>
                  <label htmlFor="trip-budget-amount">
                    Amount
                    <input
                      id="trip-budget-amount"
                      min="0"
                      onChange={(event) => setBudgetAmount(event.target.value)}
                      placeholder="850"
                      step="0.01"
                      type="number"
                      value={budgetAmount}
                    />
                  </label>
                </div>
                <div className="form-actions">
                  <button
                    className="primary-button"
                    disabled={
                      isAddingBudgetItem ||
                      !budgetTitle.trim() ||
                      !budgetCategory.trim() ||
                      !budgetAmount.trim()
                    }
                    type="submit"
                  >
                    {isAddingBudgetItem ? "Adding..." : "Add Budget Item"}
                  </button>
                </div>
              </form>

              {budgetError && (
                <p className="preferences-message error-text">{budgetError}</p>
              )}

              {isBudgetItemsLoading && (
                <div
                  className="budget-items-list"
                  aria-label="Loading trip budget items"
                >
                  <div className="skeleton-card note-skeleton" />
                </div>
              )}

              {!isBudgetItemsLoading && isBudgetItemsError && (
                <p className="preferences-message error-text">
                  Could not load budget items.
                </p>
              )}

              {!isBudgetItemsLoading && !isBudgetItemsError && (
                <div className="budget-items-list">
                  {budgetItems.length === 0 ? (
                    <p className="notes-empty">No budget items yet.</p>
                  ) : (
                    budgetItems.map((item) => (
                      <article className="budget-item" key={item.id}>
                        {editingBudgetItemId === item.id ? (
                          <form
                            className="inline-budget-edit-form"
                            onSubmit={handleUpdateBudgetItem}
                          >
                            <div className="metadata-form-grid">
                              <label htmlFor={`budget-title-${item.id}`}>
                                Title
                                <input
                                  id={`budget-title-${item.id}`}
                                  onChange={(event) =>
                                    setEditBudgetTitle(event.target.value)
                                  }
                                  type="text"
                                  value={editBudgetTitle}
                                />
                              </label>
                              <label htmlFor={`budget-category-${item.id}`}>
                                Category
                                <input
                                  id={`budget-category-${item.id}`}
                                  onChange={(event) =>
                                    setEditBudgetCategory(event.target.value)
                                  }
                                  type="text"
                                  value={editBudgetCategory}
                                />
                              </label>
                              <label htmlFor={`budget-amount-${item.id}`}>
                                Amount
                                <input
                                  id={`budget-amount-${item.id}`}
                                  min="0"
                                  onChange={(event) =>
                                    setEditBudgetAmount(event.target.value)
                                  }
                                  step="0.01"
                                  type="number"
                                  value={editBudgetAmount}
                                />
                              </label>
                            </div>
                            <div className="note-actions">
                              <button
                                className="primary-button compact-button"
                                disabled={
                                  isUpdatingBudgetItem ||
                                  !editBudgetTitle.trim() ||
                                  !editBudgetCategory.trim() ||
                                  !editBudgetAmount.trim()
                                }
                                type="submit"
                              >
                                {isUpdatingBudgetItem &&
                                updatingBudgetItemId === item.id
                                  ? "Saving..."
                                  : "Save"}
                              </button>
                              <button
                                className="secondary-button compact-button"
                                disabled={isUpdatingBudgetItem}
                                onClick={cancelEditBudgetItem}
                                type="button"
                              >
                                Cancel
                              </button>
                            </div>
                          </form>
                        ) : (
                          <>
                            <div>
                              <strong>{item.title}</strong>
                              <span>{item.category}</span>
                            </div>
                            <strong>{formatCurrency(Number(item.amount || 0))}</strong>
                            <div className="note-actions">
                              <button
                                className="secondary-button compact-button"
                                disabled={
                                  isDeletingBudgetItem ||
                                  isUpdatingBudgetItem
                                }
                                onClick={() =>
                                  startEditBudgetItem(
                                    item.id,
                                    item.title,
                                    item.category,
                                    Number(item.amount || 0)
                                  )
                                }
                                type="button"
                              >
                                Edit
                              </button>
                              <button
                                className="secondary-button compact-button note-delete-button"
                                disabled={
                                  isDeletingBudgetItem ||
                                  isUpdatingBudgetItem
                                }
                                onClick={() =>
                                  void handleDeleteBudgetItem(item.id)
                                }
                                type="button"
                              >
                                {isDeletingBudgetItem &&
                                deletingBudgetItemId === item.id
                                  ? "Deleting..."
                                  : "Delete"}
                              </button>
                            </div>
                          </>
                        )}
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
