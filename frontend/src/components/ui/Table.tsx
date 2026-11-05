import { forwardRef } from 'react';
import clsx from 'clsx';

export const Table = forwardRef<HTMLTableElement, React.TableHTMLAttributes<HTMLTableElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <table ref={ref} className={clsx('table', className)} {...props}>
        {children}
      </table>
    );
  }
);

Table.displayName = 'Table';

export const TableHeader = forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <thead ref={ref} className={clsx('', className)} {...props}>
        {children}
      </thead>
    );
  }
);

TableHeader.displayName = 'TableHeader';

export const TableBody = forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <tbody ref={ref} className={clsx('', className)} {...props}>
        {children}
      </tbody>
    );
  }
);

TableBody.displayName = 'TableBody';

export const TableRow = forwardRef<HTMLTableRowElement, React.HTMLAttributes<HTMLTableRowElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <tr ref={ref} className={clsx('', className)} {...props}>
        {children}
      </tr>
    );
  }
);

TableRow.displayName = 'TableRow';

export const TableCell = forwardRef<HTMLTableCellElement, React.ThHTMLAttributes<HTMLTableCellElement> & React.TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <td ref={ref} className={clsx('', className)} {...props}>
        {children}
      </td>
    );
  }
);

TableCell.displayName = 'TableCell';

export const TableHead = forwardRef<HTMLTableCellElement, React.ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <th ref={ref} className={clsx('', className)} {...props}>
        {children}
      </th>
    );
  }
);

TableHead.displayName = 'TableHead';