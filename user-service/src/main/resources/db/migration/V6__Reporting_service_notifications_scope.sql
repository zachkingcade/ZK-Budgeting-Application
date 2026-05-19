UPDATE service_permissions
SET allowed_audiences = allowed_audiences || ',user-service',
    allowed_scopes = allowed_scopes || ',notifications.write'
WHERE service_name = 'reporting-service';
