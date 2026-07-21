import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import CargoTicketPrintLabel from '../components/CargoTicketPrintLabel';
import '../styles/cargoTicketPrint.css';

const HOST_ID = 'cargo-ticket-print-host';

let printRoot = null;
let cleanupTimer = null;

function ensurePrintRoot() {
    let host = document.getElementById(HOST_ID);
    if (!host) {
        host = document.createElement('div');
        host.id = HOST_ID;
        host.className = 'cargo-print-host';
        document.body.appendChild(host);
    }
    if (!printRoot) {
        printRoot = createRoot(host);
    }
    return printRoot;
}

function clearPrintHost() {
    window.removeEventListener('afterprint', clearPrintHost);
    if (cleanupTimer != null) {
        clearTimeout(cleanupTimer);
        cleanupTimer = null;
    }
    if (printRoot) {
        printRoot.render(null);
    }
}

/**
 * Renders the sticker off-screen and opens the browser print dialog.
 * @param {object} ticket cargo ticket response
 * @param {{ pieceCount?: number|null }} [options]
 */
export function printCargoTicket(ticket, options = {}) {
    if (!ticket) return;

    clearPrintHost();
    const root = ensurePrintRoot();
    root.render(createElement(CargoTicketPrintLabel, {
        ticket,
        pieceCount: options.pieceCount ?? null
    }));

    window.addEventListener('afterprint', clearPrintHost);

    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            window.print();
            cleanupTimer = setTimeout(clearPrintHost, 60_000);
        });
    });
}
