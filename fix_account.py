import os

account_file = "staff-service/src/main/java/com/ralsei/staff/model/Account.java"
with open(account_file, "r") as f:
    content = f.read()

# Add missing UserDetails methods
methods_to_add = """
    @Override
    public boolean isEnabled() { return true; }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
"""

# replace if not already added
if "boolean isEnabled()" not in content:
    content = content.replace("public Collection<? extends GrantedAuthority> getAuthorities() {", methods_to_add + "\n    public Collection<? extends GrantedAuthority> getAuthorities() {")

# remove duplicate @Override if any (from previous runs)
content = content.replace("@Override\n    @Override", "@Override")

with open(account_file, "w") as f:
    f.write(content)
