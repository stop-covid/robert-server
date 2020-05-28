const options : Intl.DateTimeFormatOptions = {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
};

class DatePrinter {
    static  stringify(d: Date | string) : String {
        if(!d) return 'undefined date';
        return new Date(d).toLocaleDateString(undefined, options)
    }
}

export default DatePrinter;
