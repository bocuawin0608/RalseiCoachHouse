import { createContext, useContext } from 'react';

const SidebarContext = createContext(null);

export function SidebarProvider({ value, children }) {
    return (
        <SidebarContext.Provider value={value}>
            {children}
        </SidebarContext.Provider>
    );
}

export function useSidebar() {
    const ctx = useContext(SidebarContext);
    if (!ctx) {
        throw new Error('useSidebar must be used within SidebarProvider');
    }
    return ctx;
}
