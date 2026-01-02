update person.person
set firstname = firstname || '-' || :suffix
where modifieddate < :cutoff
returning firstname, modifieddate