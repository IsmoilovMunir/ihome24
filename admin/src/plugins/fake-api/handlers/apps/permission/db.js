export const db = {
  permissions: [
    {
      id: 1,
      name: 'Управление',
      assignedTo: ['administrator'],
      createdDate: '14 Apr 2021, 8:43 PM',
    },
    {
      id: 2,
      assignedTo: ['administrator'],
      name: 'Управление биллингом и ролями',
      createdDate: '16 Sep 2021, 5:20 PM',
    },
    {
      id: 3,
      name: 'Добавление и удаление пользователей',
      createdDate: '14 Oct 2021, 10:20 AM',
      assignedTo: ['administrator', 'manager'],
    },
    {
      id: 4,
      name: 'Планирование проектов',
      createdDate: '14 Oct 2021, 10:20 AM',
      assignedTo: ['administrator', 'users', 'support'],
    },
    {
      id: 5,
      name: 'Управление email-последовательностями',
      createdDate: '23 Aug 2021, 2:00 PM',
      assignedTo: ['administrator', 'users', 'support'],
    },
    {
      id: 6,
      name: 'Коммуникация с клиентами',
      createdDate: '15 Apr 2021, 11:30 AM',
      assignedTo: ['administrator', 'manager'],
    },
    {
      id: 7,
      name: 'Только просмотр',
      createdDate: '04 Dec 2021, 8:15 PM',
      assignedTo: ['administrator', 'restricted-user'],
    },
    {
      id: 8,
      name: 'Финансовое управление',
      createdDate: '25 Feb 2021, 10:30 AM',
      assignedTo: ['administrator', 'manager'],
    },
    {
      id: 9,
      name: 'Управление задачами других',
      createdDate: '04 Nov 2021, 11:45 AM',
      assignedTo: ['administrator', 'support'],
    },
  ],
}
