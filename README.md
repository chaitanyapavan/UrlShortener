# UrlShortener
A Spring based UrlShortener. Takes a Url in json format and returns its shortened form. The shortened form can be used to redirect to the original url.

# Giving the URL
method = POST, Format = Json, just give a single key "url" : "your url".
Example,
{
 "url" : "twitter.com"
}

This gives the response - a shortened url.



