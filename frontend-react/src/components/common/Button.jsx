import styles from './Button.module.css';

export default function Button({ 
    children, 
    variant = 'primary', 
    size = 'medium',    
    iconOnly = false,   
    className = '',      
    ...rest              
}) {
    const btnClass = `
        ${styles.btn} 
        ${styles[variant]} 
        ${iconOnly ? styles.iconOnly : styles[size]} 
        ${className}
    `.trim();

    return (
        <button className={btnClass} {...rest}>
            {children}
        </button>
    );
}