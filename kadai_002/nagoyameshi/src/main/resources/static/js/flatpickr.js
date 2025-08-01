let maxDate = new Date();
maxDate.setMonth(maxDate.getMonth() + 3);

flatpickr('#reservationDateTimePicker', {
  enableTime: true,
  dateFormat: "Y-m-d H:i",
  locale: 'ja',
  minDate: 'today',
  maxDate: maxDate, 
  onClose: function(selectedDates, dateStr) {
    if (selectedDates.length > 0) {
      const selectedDate = selectedDates[0];
      const date = selectedDate.toISOString().split("T")[0];
      const time = selectedDate.toTimeString().substring(0, 5);

      document.querySelector("input[name='reservationDate']").value = date;
      document.querySelector("input[name='reservationTime']").value = time;
    } else {
      document.querySelector("input[name='reservationDate']").value = '';
      document.querySelector("input[name='reservationTime']").value = '';
    }
  }
});
