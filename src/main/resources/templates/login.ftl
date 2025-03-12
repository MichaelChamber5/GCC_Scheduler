<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>
        Login
    </title>
</head>
<body>
<h1>Login</h1>
<form action="/login" method="post">
    <div>
        <label for="username">Username:</label>
        <input type="text" id="username" name = "username" required />
    </div>
    <div>
        <label for="confirmedPassword">Confirm Password:</label>
        <input type="password" id="confirmedPassword" name = "confirmPassword" required />
    </div>
    <div>
        <input type="submit" value="Login" />
    </div>

</form>
<p>Don't have an account? <a href="/register">Register here</a>.</p>
</body>
</html>