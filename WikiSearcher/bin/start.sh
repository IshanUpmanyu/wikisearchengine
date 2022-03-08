resolve_relative_path() (
    # If the path is a directory, we just need to 'cd' into it and print the new path.
    if [ -d "$1" ]; then
        cd "$1" || return 1
        pwd
    # If the path points to anything else, like a file or FIFO
    elif [ -e "$1" ]; then
        # Strip '/file' from '/dir/file'
        # We only change the directory if the name doesn't match for the cases where
        # we were passed something like 'file' without './'
        if [ ! "${1%/*}" = "$1" ]; then
            cd "${1%/*}" || return 1
        fi
        # Strip all leading slashes upto the filename
        echo "$(pwd)/${1##*/}"
    else
        return 1 # Failure, neither file nor directory exists.
    fi
)

script_dir_rel_path=$(dirname "$0")
echo $script_dir_rel_path
script_dir=$(resolve_relative_path $script_dir_rel_path)
conf_folder="$script_dir/../conf/"
log_file="$script_dir/../logs/app.log"
java_command="java -jar  $script_dir/../lib/WikiSearcher-0.0.1-SNAPSHOT.jar --spring.config.location=$conf_folder"
echo $java_command
nohup $java_command >> $log_file 2>&1 &
if [ $? -eq 0 ]; then
        echo "WikiSearcher started successfully!"
else
         echo "WikiSearcher failed to start. check logs!"
fi
