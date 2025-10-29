import { Ticket } from "../../features/tickets/models/ticket";

export type Replace<T, R extends Partial<Record<keyof T, any>>> =
  Omit<T, keyof R> & R;

export type HydratedTicket = Replace<Ticket, {
  createdOn: Date;
  lastChange: Date;
}>;
